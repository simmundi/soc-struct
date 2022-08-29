package pl.edu.icm.board.pdyn1;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class ExportedId {
    int pdyn2Id;
    int pdyn1Id;

    public ExportedId() {
    }

    public int getPdyn2Id() {
        return pdyn2Id;
    }

    public void setPdyn2Id(int pdyn2Id) {
        this.pdyn2Id = pdyn2Id;
    }

    public int getPdyn1Id() {
        return pdyn1Id;
    }

    public void setPdyn1Id(int pdyn1Id) {
        this.pdyn1Id = pdyn1Id;
    }
}
