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
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@ApplicationScoped
@RegisterForReflection
public class UserCompanyRoleRepository {

    @ConfigProperty(name = "userCompanyRole.table.name")
    String tableName;

    @Inject
    ClientFactory clientFactory;

    private DynamoDbTable<UserCompanyRole> getTable() {
        return clientFactory
                .getEnhancedDynamoClient()
                .table(tableName, Schemas.USER_COMPANY_ROLE_SCHEMA);
    }

    public void save(UserCompanyRole role) {
        DynamoDBUtils.putItem(getTable(), role);
    }

    public UserCompanyRole getByUserAndCompany(String userId, String companyId) {
        return getTable().getItem(Key.builder()
                .partitionValue(userId)
                .sortValue(companyId)
                .build());
    }

    public void delete(String userId, String companyId) {
        getTable().deleteItem(Key.builder()
                .partitionValue(userId)
                .sortValue(companyId)
                .build());
    }

    /**
     * ✅ Query all roles for a given companyId using the GSI (companyId-index)
     */
    public List<UserCompanyRole> getByCompanyId(String companyId) {
        var index = getTable().index("companyId-index");

        var queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(companyId).build()
        );

        List<UserCompanyRole> results = new ArrayList<>();
        index.query(queryConditional).forEach(page -> {
            page.items().forEach(results::add);
        });

        return results;
    }

    /**
     * ✅ Query all roles for a given userId (partition key)
     */
    public List<UserCompanyRole> getAllByUserId(String userId) {
        var queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(userId).build()
        );

        List<UserCompanyRole> results = new ArrayList<>();
        getTable().query(queryConditional).forEach(page -> {
            page.items().forEach(results::add);
        });

        return results;
    }
}
