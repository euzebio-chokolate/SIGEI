package ac.gov.br.sigei_backend.models;

import jakarta.persistence.*;

@Entity
public class Aldeia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;

    @ManyToOne
    @JoinColumn(name = "terra_indigena_id")
    private TerraIndigena terraIndigena;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TerraIndigena getTerraIndigena() {
        return terraIndigena;
    }

    public void setTerraIndigena(TerraIndigena terraIndigena) {
        this.terraIndigena = terraIndigena;
    }
}
