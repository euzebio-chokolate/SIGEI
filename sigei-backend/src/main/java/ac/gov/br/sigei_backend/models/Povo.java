package ac.gov.br.sigei_backend.models;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Povo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @OneToMany(mappedBy = "povo")
    private List<Aldeia> aldeias = new ArrayList<>();

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

    public List<Aldeia> getAldeias() {
        return aldeias;
    }

    public void setAldeias(List<Aldeia> aldeias) {
        this.aldeias = aldeias;
    }
}