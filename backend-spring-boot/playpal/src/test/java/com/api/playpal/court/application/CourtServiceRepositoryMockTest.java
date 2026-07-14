package com.api.playpal.court.application;

import com.api.playpal.branch.domain.Branch;
import com.api.playpal.branch.infrastructure.BranchRepositoryImp;
import com.api.playpal.court.domain.Court;
import com.api.playpal.court.infrastructure.CourtRepositoryImp;
import com.api.playpal.user.domain.User;
import com.api.playpal.utils.StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
class CourtServiceRepositoryMockTest {

    @Mock
    private CourtRepositoryImp courtRepository;

    @Mock
    private BranchRepositoryImp branchRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private CourtService courtService;

    private Branch someBranch() {
        User provider = new User("prov", "prov@mail.com", "secret", "provider");
        provider.setId("prov-1");
        return new Branch("Sede Centro", "Cordoba", "Calle 1", provider);
    }

    @Test
    void savePersistsCourtAndUpdatesBranchWithoutDatabase() throws IOException {
        Branch branch = someBranch();
        when(branchRepository.findById("branch-1")).thenReturn(Optional.of(branch));
        when(courtRepository.save(any(Court.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(branchRepository.save(any(Branch.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Court saved = courtService.save("futbol", "futbol 5", 1, 1000L, "Cancha techada", "08:00-22:00", "branch-1", null);

        assertEquals("Sede Centro", saved.getBranchName());
        assertEquals(1, branch.getCourts().size());
        verify(courtRepository, times(2)).save(any(Court.class));
        verify(branchRepository).save(branch);
    }

    @Test
    void saveFailsWhenBranchDoesNotExist() {
        when(branchRepository.findById("branch-404")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> courtService.save("futbol", "futbol 5", 1, 1000L, "Cancha techada", "08:00-22:00", "branch-404", null));

        assertEquals("Branch not found", exception.getMessage());
        verify(courtRepository, never()).save(any(Court.class));
    }

    @Test
    void findByIdDelegatesToRepository() {
        Court court = new Court("futbol 5", "futbol", 1, 1000L, "Cancha techada", "08:00-22:00");
        court.setId("court-1");
        when(courtRepository.findById("court-1")).thenReturn(Optional.of(court));

        Optional<Court> found = courtService.findById("court-1");

        assertTrue(found.isPresent());
        verify(courtRepository).findById("court-1");
    }

    @Test
    void findAllWithoutFiltersDelegatesToRepository() {
        when(courtRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        Page<Court> page = courtService.findAll(0, null, null);

        assertTrue(page.isEmpty());
        verify(courtRepository).findAll(any(Pageable.class));
    }

    @Test
    void findAllBySportDelegatesToRepository() {
        when(courtRepository.findBySportsContains(eq("futbol"), any(Pageable.class))).thenReturn(Page.empty());

        Page<Court> page = courtService.findAll(0, "futbol", null);

        assertTrue(page.isEmpty());
        verify(courtRepository).findBySportsContains(eq("futbol"), any(Pageable.class));
    }

    @Test
    void deleteByIdDelegatesToRepository() {
        courtService.deleteById("court-1");

        verify(courtRepository).deleteById("court-1");
    }
}
