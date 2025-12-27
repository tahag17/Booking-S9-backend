package fr.ensaetud.Booking_back.user.application;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.filter.FieldsFilter;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.json.mgmt.users.User;
import com.auth0.net.Response;
import com.auth0.net.TokenRequest;
import fr.ensaetud.Booking_back.infrastructure.config.SecurityUtils;
import fr.ensaetud.Booking_back.user.application.dto.ReadUserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class Auth0Service {

    @Value("${okta.oauth2.client-id}")
    private String clientId;

    @Value("${okta.oauth2.client-secret}")
    private String clientSecret;

    @Value("${okta.oauth2.issuer}")
    private String domain;

    @Value("${application.auth0.role-landlord-id}")
    private String landlordRoleId;

    private String getAccessToken() throws Auth0Exception {
        System.out.println("[Auth0] Requesting access token...");
        System.out.println("[Auth0] Domain = " + domain);

        AuthAPI authAPI = AuthAPI.newBuilder(domain, clientId, clientSecret).build();
        TokenRequest tokenRequest = authAPI.requestToken(domain + "/api/v2/");

        TokenHolder holder = tokenRequest.execute().getBody();

        System.out.println("[Auth0] Access token received, expires in "
                + holder.getExpiresIn() + " seconds");

        return holder.getAccessToken();
    }

    private void assignRoleById(
            String accessToken,
            String email,
            UUID publicId,
            String roleId
    ) throws Auth0Exception {

        System.out.println("[Auth0] Assigning role...");
        System.out.println("[Auth0] Email = " + email);
        System.out.println("[Auth0] PublicId = " + publicId);
        System.out.println("[Auth0] RoleId = " + roleId);

        ManagementAPI managementAPI =
                ManagementAPI.newBuilder(domain, accessToken).build();

        Response<List<User>> response =
                managementAPI.users().listByEmail(email, new FieldsFilter()).execute();

        System.out.println("[Auth0] Users found by email = " + response.getBody().size());

        User user = response.getBody()
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new UserException(
                                String.format(
                                        "Cannot find user with public id %s in Auth0",
                                        publicId
                                )
                        )
                );

        System.out.println("[Auth0] Auth0 user id = " + user.getId());

        managementAPI
                .roles()
                .assignUsers(roleId, List.of(user.getId()))
                .execute();

        System.out.println("[Auth0] Role successfully assigned ✔");
    }

    public void addLandlordRoleToUser(ReadUserDTO readUserDTO) {

        System.out.println("[Auth0] Checking landlord role for user "
                + readUserDTO.publicId());

        if (readUserDTO.authorities()
                .stream()
                .noneMatch(role -> role.equals(SecurityUtils.ROLE_LANDLORD))) {

            try {
                String accessToken = this.getAccessToken();

                this.assignRoleById(
                        accessToken,
                        readUserDTO.email(),
                        readUserDTO.publicId(),
                        landlordRoleId
                );

            } catch (Auth0Exception e) {

                // 🔴 THIS IS THE IMPORTANT PART
                System.out.println("[Auth0] ERROR while assigning role");
                System.out.println("[Auth0] Message: " + e.getMessage());
                e.printStackTrace(); // shows the real Auth0 error

                throw new UserException(
                        String.format(
                                "not possible to assign %s to %s",
                                landlordRoleId,
                                readUserDTO.publicId()
                        )
                );
            }
        } else {
            System.out.println("[Auth0] User already has LANDLORD role");
        }
    }
}
