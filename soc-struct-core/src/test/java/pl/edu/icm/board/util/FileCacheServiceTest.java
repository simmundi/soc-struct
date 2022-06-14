package pl.edu.icm.board.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.io.orc.OrcStoreService;
import pl.edu.icm.trurl.store.Store;

import java.io.IOException;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FileCacheServiceTest {

    @Mock
    Store store;

    @Mock
    Consumer<Store> consumer;

    @Mock
    OrcStoreService orcStoreService;

    @InjectMocks
    FileCacheService fileCacheService;

    @Test
    @DisplayName("Should execute store consumer and save the results")
    void computeIfAbsent__absent() throws IOException {
        // execute
        fileCacheService.computeIfAbsent("test", store, consumer);

        // assert
        verify(consumer).accept(store);
        verify(orcStoreService).write(eq(store), endsWith("test.orc"));
    }

    @Test
    @DisplayName("Should not execute store consumer and read the store back from file")
    void computeIfAbsent__present() throws IOException {
        // given
        Mockito.when(orcStoreService.fileExists(anyString())).thenReturn(true);

        // execute
        fileCacheService.computeIfAbsent("test", store, consumer);

        // assert
        verify(consumer, Mockito.never()).accept(store);
        verify(orcStoreService).read(eq(store), endsWith("test.orc"));
    }

}
