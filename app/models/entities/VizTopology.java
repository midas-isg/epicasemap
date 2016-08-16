package models.entities;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.Objects;

@Entity
@Table(name = "viz_topology")
public class VizTopology implements models.entities.Entity {
	private Long id;
    private Long vizId;
    private String topoJson;

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Column(unique = true, nullable = false)
    public Long getVizId() {
        return vizId;
    }

    public void setVizId(Long vizId) {
        this.vizId = vizId;
    }

    @Column(length = 10_485_760, nullable = false)
    public String getTopoJson() {
        return topoJson;
    }

    public void setTopoJson(String topoJson) {
        this.topoJson = topoJson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VizTopology that = (VizTopology) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(vizId, that.vizId) &&
                Objects.equals(topoJson, that.topoJson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, vizId, topoJson);
    }
}
