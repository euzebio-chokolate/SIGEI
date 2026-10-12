# Demonstração do SIGEI sem cadastrar cartão

Requisito: demonstrar o sistema com custo zero e sem fornecer cartão,
inclusive para verificação. Consulta aos provedores em 12/10/2026.

## Escolha conforme a apresentação

| Caminho | Computador pode desligar? | Atende sem cartão? | Comportamento |
| --- | --- | --- | --- |
| SIGEI local + Cloudflare Quick Tunnel | Não | Sim, sem cadastro | Link HTTPS temporário; usa a capacidade do computador e sua conexão. |
| Vercel Hobby + Render Free + Neon Free | Sim | Depende da verificação do Render | Link hospedado; API suspende após 15 minutos sem tráfego. |

O primeiro caminho é um acesso público temporário ao sistema local, não uma
hospedagem independente do computador. É adequado a uma apresentação ao vivo
quando o computador pode permanecer ligado e conectado. O link muda ao reiniciar
o túnel. O [Quick Tunnel da Cloudflare](https://developers.cloudflare.com/tunnel/get-started/quick-tunnels/)
dispensa conta e fornece um endereço aleatório `trycloudflare.com` com HTTPS.

O segundo caminho tem o passo a passo em
[HOSPEDAGEM_GRATUITA.md](HOSPEDAGEM_GRATUITA.md). O plano gratuito do Render
existe, mas [a verificação pode solicitar cartão](https://community.render.com/t/the-deployement-of-a-web-service-fails/36005).
Se isso ocorrer, essa conta não serve à restrição deste projeto. Não cadastrar
cartão para tentar concluir esse caminho.

## Preparar o acesso temporário

1. Executar banco e backend localmente, com os dados escolhidos para a demonstração.
   Pode usar o Compose local existente ou a API na IDE. Confirmar a porta da API
   e que `/actuator/health` retorna `{"status":"UP"}`.
2. Usar o frontend de produção com Nginx. Na pasta `sigei-backend`, executar:

```bash
# Exemplo: API publicada no host em 8081. Trocar se a porta local for outra.
API_UPSTREAM=http://host.docker.internal:8081 FRONTEND_PORT=4200 \
  docker compose -f ../sigei-frontend/compose.yml up -d --build
```

O Compose do frontend já configura `host.docker.internal` para Linux.
Se a porta 4200 estiver ocupada pelo Angular em desenvolvimento, usar
`FRONTEND_PORT=4201` e ajustar também o destino do túnel abaixo.
Confirmar que `http://localhost:4200` abre o mapa e que
`http://localhost:4200/api/actuator/health` responde com JSON.

3. Instalar `cloudflared` conforme os
   [downloads oficiais](https://developers.cloudflare.com/cloudflare-one/networks/connectors/cloudflare-tunnel/downloads/).
   Abrir um terminal e executar:

```bash
cloudflared tunnel --url http://localhost:4200
```

4. Usar o endereço HTTPS `https://...trycloudflare.com` exibido pelo comando.
   Manter o terminal, backend, banco e frontend em execução durante a apresentação.
5. Abrir esse link pelo celular ou em outro computador e testar mapa, login,
   alteração da senha inicial, cadastro, dashboard e foto, conforme o roteiro.
6. Ao terminar, encerrar `cloudflared` com `Ctrl+C` para retirar o acesso público.

Encaminhar o túnel ao frontend, que já tem proxy `/api`, preserva a mesma origem
exigida pelos cookies e pelo CSRF do SIGEI. Manter `SIGEI_COOKIE_SECURE=true`
e `environment.apiUrl='/api'`. Não usar a porta da API como destino do túnel.

Para fluidez, usar o build de produção, evitar importações grandes durante a
fala e testar a conexão do local da apresentação. O tempo de resposta depende
da máquina, da conexão e dos dados; não foi medido em um túnel nesta sessão.

## Opções que exigem atenção ao requisito

- Oracle Cloud: o cadastro normal pede cartão; não é a recomendação para este caso.
- [Northflank](https://northflank.com/docs/v1/application/billing/pricing-on-northflank):
  exige método de pagamento inclusive no Sandbox.
- [Hugging Face Spaces](https://huggingface.co/docs/hub/spaces-overview): a documentação
  atual exige plano pago para criar novos Spaces com execução Docker/Gradio,
  apesar de o hardware CPU Basic aparecer com custo horário zero.
- [Railway](https://docs.railway.com/pricing/free-trial): permite avaliação sem cartão,
  mas o crédito inicial expira e o plano gratuito posterior tem franquia mensal
  pequena. Não equivale a hospedagem ilimitada para esta API Java.

