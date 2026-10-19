# Chaves dos dados sensíveis

CPF, matrícula e número do contrato dos professores são gravados cifrados
(AES-256-GCM), também nas tabelas de auditoria. Este guia explica as chaves
envolvidas e os procedimentos de configuração, troca e recuperação.

## Como funciona

A cifra é em **envelope**:

| Chave | Onde fica | Para que serve |
| --- | --- | --- |
| Chave de dados | Tabela `chave_dados`, sempre cifrada | Cifra os valores. Nunca muda nas trocas de chave mestra. |
| Chave mestra (`SIGEI_CHAVE_DADOS_SENSIVEIS`) | Variável de ambiente | Cifra a chave de dados. |
| Chave pública de recuperação (`SIGEI_CHAVE_RECUPERACAO_PUBLICA`) | Variável de ambiente | Guarda uma segunda cópia cifrada da chave de dados. |
| Chave privada de recuperação | **Fora do servidor**, com os responsáveis | Abre essa segunda cópia se a chave mestra se perder. |

Consequências:

- **Trocar a chave mestra** recifra só o registro de `chave_dados`. Os valores
  gravados não mudam.
- **Perder a chave mestra** não perde os dados, desde que a chave de recuperação
  tenha sido configurada e a chave privada esteja guardada.
- A aplicação **não sobe** com uma chave que não abre os dados. Ela confere a
  chave de dados com uma amostra de cada coluna cifrada, para nunca gravar
  valores novos com a chave errada.

Na primeira inicialização com a migração `V11`, a aplicação registra como chave
de dados a chave mestra atual, que é a que já cifra os dados existentes. Por isso
nada é recifrado. Depois da primeira troca de chave mestra, a chave de dados
deixa de estar em qualquer variável de ambiente.

O código está em `config/criptografia` (`GerenciadorChaveDados`, `EnvelopeChave`,
`CriptografiaDadosSensiveis`). Os testes estão em `GerenciadorChaveDadosTest`.

## Configurar a chave de recuperação (uma vez)

Faça isso logo após a implantação. Sem a chave de recuperação, a aplicação
registra um aviso a cada inicialização, e perder a chave mestra torna os dados
irrecuperáveis.

1. Gere o par de chaves num computador confiável, **fora do servidor**. A senha
   protege o arquivo da chave privada:

   ```bash
   openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:4096 -aes256 \
       -out sigei-recuperacao-privada.pem
   ```

2. Extraia a chave pública numa linha e configure-a em
   `SIGEI_CHAVE_RECUPERACAO_PUBLICA` (no `.env` ou no painel da hospedagem):

   ```bash
   openssl pkey -in sigei-recuperacao-privada.pem -pubout -outform DER | base64 -w0
   ```

3. Reinicie a aplicação. O log mostra "Cópia de recuperação da chave de dados
   atualizada".
4. Guarde `sigei-recuperacao-privada.pem` e a senha **separados** e com mais de
   um responsável. Por exemplo, o arquivo num cofre ou pendrive cifrado e a senha
   num gerenciador de senhas institucional. Nunca coloque a chave privada no
   repositório, no servidor ou em backups do banco.

A chave pública precisa ter pelo menos 3072 bits. Trocar a chave pública só
recifra a cópia de recuperação. Repita os passos acima se a chave privada se
perder ou for exposta.

## Trocar a chave mestra

Use quando a chave mestra foi exposta ou como rotina periódica.

1. Gere a nova: `openssl rand -base64 32`.
2. Configure:
   - `SIGEI_CHAVE_DADOS_SENSIVEIS` = a **nova** chave;
   - `SIGEI_CHAVE_DADOS_SENSIVEIS_ANTERIOR` = a chave **antiga**.
3. Reinicie. O log mostra "Chave mestra trocada: a chave de dados foi recifrada
   com a nova".
4. Remova `SIGEI_CHAVE_DADOS_SENSIVEIS_ANTERIOR` e reinicie de novo. Enquanto ela
   estiver configurada sem necessidade, o log avisa que já pode ser removida.

## Recuperar após perder a chave mestra

1. Gere uma nova chave mestra (`openssl rand -base64 32`) e configure-a em
   `SIGEI_CHAVE_DADOS_SENSIVEIS`.
2. Converta a chave privada para uma linha. Será pedida a senha do arquivo:

   ```bash
   openssl pkcs8 -topk8 -nocrypt -in sigei-recuperacao-privada.pem -outform DER | base64 -w0
   ```

3. Configure essa linha em `SIGEI_CHAVE_RECUPERACAO_PRIVADA` e reinicie. O log
   mostra "Chave de dados recuperada pela chave de recuperação".
4. **Remova `SIGEI_CHAVE_RECUPERACAO_PRIVADA` imediatamente** e reinicie. A nova
   chave mestra passa a abrir a chave de dados sozinha.
5. Como a chave privada passou pelo servidor, gere um novo par de recuperação
   (seção anterior) e descarte o antigo.

## Backups

Um backup do banco inclui `chave_dados`, mas ela está cifrada. Para restaurar um
backup com os dados sensíveis legíveis, é preciso a chave mestra vigente na data
do backup ou a chave privada de recuperação. Guarde a chave mestra junto com os
segredos da hospedagem, nunca junto com os backups do banco.

Ao restaurar um banco em outro ambiente, use a chave mestra do ambiente de
origem. Se usar outra, a aplicação não sobe e indica qual variável conferir.

## Mensagens na inicialização

| Mensagem | O que fazer |
| --- | --- |
| "SIGEI_CHAVE_DADOS_SENSIVEIS não abre a chave de dados gravada no banco" | A chave mestra não é a deste banco. Informe a antiga em `SIGEI_CHAVE_DADOS_SENSIVEIS_ANTERIOR` ou recupere com a chave privada. |
| "A chave de dados não decifra os dados sensíveis já gravados" | O banco tem valores cifrados com outra chave, por exemplo restaurado de outro ambiente. Use a chave mestra de origem. |
| Aviso "Sem SIGEI_CHAVE_RECUPERACAO_PUBLICA" | Configure a chave de recuperação. |
| Aviso "Remova SIGEI_CHAVE_RECUPERACAO_PRIVADA" ou "...ANTERIOR já pode ser removida" | Remova a variável indicada e reinicie. |

## O que continua exigindo recifrar

Se a **chave de dados** for exposta (o banco e a chave mestra vazaram juntos),
trocar a chave mestra não basta: os valores precisam ser recifrados com uma nova
chave de dados, e o `cpf_hash` recalculado. Esse procedimento ainda não está
automatizado.
