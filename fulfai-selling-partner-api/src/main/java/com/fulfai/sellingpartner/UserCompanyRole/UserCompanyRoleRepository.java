package com.fulfai.sellingpartner.UserCompanyRole;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fulfai.common.dynamodb.ClientFactory;
import com.fulfai.common.dynamodb.DynamoDBUtils;
import com.fulfai.sellingpartner.Schemas;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@ApplicationScoped
@RegisterForReflection
public class UserCompanyRoleRepository {

    @ConfigProperty(name = "userCompanyRole.table.name")
    String tableName;

    @Inject
    ClientFactory clientFactory;

    private DynamoDbTable<UserCompanyRole> table() {
        return clientFactory
                .getEnhancedDynamoClient()
                .table(tableName, Schemas.USER_COMPANY_ROLE_SCHEMA);
    }

    /* ============================
       WRITE
    ============================= */

    public void save(UserCompanyRole role) {
        DynamoDBUtils.putItem(table(), role);
    }

    public void delete(String userId, String companyId, String branchId) {
        table().deleteItem(
                Key.builder()
                        .partitionValue(userId)
                        .sortValue(companyId + "#" + (branchId == null ? "ROOT" : branchId))
                        .build()
        );
    }

    /* ============================
       READ
    ============================= */

    public UserCompanyRole getRole(
            String userId,
            String companyId,
            String branchId
    ) {
        return table().getItem(
                Key.builder()
                        .partitionValue(userId)
                        .sortValue(companyId + "#" + (branchId == null ? "ROOT" : branchId))
                        .build()
        );
    }

    public List<UserCompanyRole> getAllByUserId(String userId) {
        QueryConditional query = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(userId).build()
        );

        List<UserCompanyRole> results = new ArrayList<>();
        table().query(query).forEach(p -> p.items().forEach(results::add));
        return results;
    }

    public List<UserCompanyRole> getByCompanyId(String companyId) {
        var index = table().index("companyId-index");

        QueryConditional query = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(companyId).build()
        );

        List<UserCompanyRole> results = new ArrayList<>();
        index.query(query).forEach(p -> p.items().forEach(results::add));
        return results;
    }


    /* ============================
   COMPANY OWNERS
============================= */

public List<UserCompanyRole> getOwnersByCompanyId(String companyId) {

    var index = table().index("companyId-index");

    QueryConditional query = QueryConditional.keyEqualTo(
            Key.builder()
                    .partitionValue(companyId)
                    .build()
    );

    List<UserCompanyRole> results = new ArrayList<>();

    index.query(query)
         .forEach(page ->
                 page.items().forEach(role -> {
                     if ("OWNER".equals(role.getRole())) {
                         results.add(role);
                     }
                 })
         );

    return results;
}

public List<UserCompanyRole> getCompanyOwners(String companyId) {

    DynamoDbIndex<UserCompanyRole> index =
            table().index("companyId-index");

    QueryConditional query =
            QueryConditional.keyEqualTo(
                    Key.builder()
                            .partitionValue(companyId)
                            .build()
            );

    List<UserCompanyRole> owners = new ArrayList<>();

    index.query(query).forEach(page -> {
        for (UserCompanyRole role : page.items()) {
            if ("OWNER".equals(role.getRole())) {
                owners.add(role);
            }
        }
    });

    return owners;
}


    
}
