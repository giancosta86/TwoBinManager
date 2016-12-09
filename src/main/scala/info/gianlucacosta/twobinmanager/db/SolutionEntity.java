/*^
  ===========================================================================
  TwoBinManager
  ===========================================================================
  Copyright (C) 2016 Gianluca Costa
  ===========================================================================
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public
  License along with this program.  If not, see
  <http://www.gnu.org/licenses/gpl-3.0.html>.
  ===========================================================================
*/

package info.gianlucacosta.twobinmanager.db;

import javax.persistence.*;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Solution entity
 */
@Entity
@Table(
        name = "Solutions",
        indexes = @Index(
                unique = false,
                columnList = "problemId"
        )
)
public class SolutionEntity {
    @Id
    private UUID id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "problemId",
            nullable = false,
            foreignKey = @ForeignKey(
                    foreignKeyDefinition = "foreign key (problemId) references Problems on delete cascade"
            )
    )
    private ProblemEntity problem;

    @ElementCollection(
            fetch = FetchType.LAZY
    )
    @CollectionTable(
            name = "Solutions_Blocks",
            joinColumns = @JoinColumn(
                    name = "solutionId",
                    nullable = false
            ),
            foreignKey = @ForeignKey(
                    foreignKeyDefinition = "foreign key (solutionId) references Solutions on delete cascade"
            ),
            uniqueConstraints = @UniqueConstraint(
                    columnNames = {"solutionId", "anchorLeft", "anchorTop"}
            )
    )
    @Column(
            nullable = false
    )
    private Set<AnchoredBlockValue> blocks;


    @Column(
            nullable = true,
            length = Integer.MAX_VALUE
    )
    private String solver;


    public SolutionEntity() {
    }

    public SolutionEntity(UUID id, ProblemEntity problem, Set<AnchoredBlockValue> blocks, String solver) {
        this.id = id;
        this.problem = problem;

        this.blocks = Collections.unmodifiableSet(
                blocks
        );

        this.solver = solver;
    }

    public UUID getId() {
        return id;
    }

    public ProblemEntity getProblem() {
        return problem;
    }

    void setProblem(ProblemEntity problem) {
        this.problem = problem;
    }

    public Set<AnchoredBlockValue> getBlocks() {
        return blocks;
    }

    public String getSolver() {
        return solver;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SolutionEntity that = (SolutionEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(problem, that.problem) &&
                Objects.equals(blocks, that.blocks) &&
                Objects.equals(solver, that.solver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, problem, blocks, solver);
    }
}
