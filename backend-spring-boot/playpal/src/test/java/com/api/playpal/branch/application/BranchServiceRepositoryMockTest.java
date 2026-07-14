package com.api.playpal.branch.application;

import com.api.playpal.branch.domain.Branch;
import com.api.playpal.branch.infrastructure.BranchRepositoryImp;
import com.api.playpal.user.domain.User;
import com.api.playpal.user.infrastructure.UserRepositoryImp;
import com.api.playpal.utils.StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
class BranchServiceRepositoryMockTest {

    @Mock
    private BranchRepositoryImp branchRepository;

    @Mock
    private UserRepositoryImp userRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private BranchService branchService;

    private User someProvider() {
        User provider = new User("prov", "prov@mail.com", "secret", "provider");
        provider.setId("prov-1");
        return provider;
    }

    @Test
    void saveCreatesBranchForProviderWithoutDatabase() throws Exception {
        User provider = someProvider();
        when(branchRepository.save(any(Branch.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Branch saved = branchService.save("Sede Centro", "Cordoba", "Calle 1", provider, null);

        assertEquals("Sede Centro", saved.getName());
        assertTrue(provider.getBranches().contains(saved));
        verify(branchRepository).save(any(Branch.class));
        verify(userRepository).save(provider);
    }

    @Test
    void saveRejectsUsersWithoutProviderRole() {
        User plainUser = new User("ana", "ana@mail.com", "secret", "user");

        Exception exception = assertThrows(Exception.class,
                () -> branchService.save("Sede Centro", "Cordoba", "Calle 1", plainUser, null));

        assertEquals("User is not a provider", exception.getMessage());
        verifyNoInteractions(branchRepository, userRepository);
    }

    @Test
    void findBranchesByProviderIdDelegatesToRepository() {
        when(branchRepository.findBranchesByProviderId("prov-1")).thenReturn(List.of());

        assertTrue(branchService.findBranchesByProviderId("prov-1").isEmpty());
        verify(branchRepository).findBranchesByProviderId("prov-1");
    }

    @Test
    void updateChangesBranchName() throws IOException {
        Branch branch = new Branch("Sede Centro", "Cordoba", "Calle 1", someProvider());
        when(branchRepository.findById("branch-1")).thenReturn(Optional.of(branch));
        when(branchRepository.save(any(Branch.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Branch updated = branchService.update("branch-1", "Sede Norte", null);

        assertEquals("Sede Norte", updated.getName());
        verify(branchRepository).save(branch);
    }

    @Test
    void deleteByIdDelegatesToRepository() {
        branchService.deleteById("branch-1");

        verify(branchRepository).deleteById("branch-1");
    }
}
