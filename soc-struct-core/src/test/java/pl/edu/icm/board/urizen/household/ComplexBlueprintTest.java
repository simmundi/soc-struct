package pl.edu.icm.board.urizen.household;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.geography.prg.model.AddressPoint;
import pl.edu.icm.board.urizen.household.model.ComplexBlueprint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplexBlueprintTest {

    @Mock
    private AddressPoint addressPoint;

    @BeforeEach
    void setup() {
        when(addressPoint.getEasting()).thenReturn(1.23f);
        when(addressPoint.getNorthing()).thenReturn(4.56f);
    }

    @Test
    void getLocationAndFrom() {
//        given
        var complexBlueprint = ComplexBlueprint.from(addressPoint);
//        execute
        var location = complexBlueprint.getLocation();
//        assert
        assertThat(location.getE()).isEqualTo(1);
        assertThat(location.getN()).isEqualTo(4);
    }

    @Test
    void getSizeAndAddAndGetHouseholdsId() {
//        given
        var complexBlueprint = ComplexBlueprint.from(addressPoint);
        for (var i = 0; i < 10; i++) {
            complexBlueprint.addHouseholdId(i);
        }
//        execute
        var householdsIds = complexBlueprint.getHouseholdsId();
//        assert
        assertThat(householdsIds).containsExactly(0,1,2,3,4,5,6,7,8,9);
        assertThat(complexBlueprint.getSize()).isEqualTo(10);
    }

}
