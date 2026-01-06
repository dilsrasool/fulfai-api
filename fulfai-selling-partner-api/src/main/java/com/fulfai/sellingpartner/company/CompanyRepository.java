package com.fulfai.sellingpartner.company;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fulfai.common.dynamodb.ClientFactory;
import com.fulfai.common.dynamodb.DynamoDBUtils;
import com.fulfai.sellingpartner.Schemas;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@ApplicationScoped
@RegisterForReflection
public class CompanyRepository {

    @ConfigProperty(name = "company.table.name")
    String tableName;

    @Inject
    ClientFactory clientFactory;

    /* =========================
       TABLE
    ========================== */

    private DynamoDbTable<Company> table() {
        return clientFactory
                .getEnhancedDynamoClient()
                .table(tableName, Schemas.COMPANY_SCHEMA);
    }

    /* =========================
       CRUD
    ========================== */

    public Company getById(String id) {
        return DynamoDBUtils.getItem(table(), id);
    }

    public void save(Company company) {
        DynamoDBUtils.putItem(table(), company);
    }

    public void delete(String id) {
        DynamoDBUtils.deleteItem(table(), id);
    }

    public String getTableName() {
        return tableName;
    }

    /* =========================
       OWNER → COMPANIES
       (GSI: ownerSub-index)
    ========================== */

    public List<Company> getAllByOwnerSub(String ownerSub) {

        DynamoDbIndex<Company> index =
                table().index("ownerSub-index");

        QueryConditional condition =
                QueryConditional.keyEqualTo(
                        Key.builder()
                           .partitionValue(ownerSub)
                           .build()
                );

        List<Company> results = new ArrayList<>();

        index.query(condition).forEach(page ->
                results.addAll(page.items())
        );

        return results;
    }

    /**
     * Legacy helper — returns first company only
     */
    public Company getByOwnerSub(String ownerSub) {
        List<Company> companies = getAllByOwnerSub(ownerSub);
        return companies.isEmpty() ? null : companies.get(0);
    }

    /* =========================
       OPTIONAL (Future)
       Join via GUID
       Requires joinCode-index
    ========================== */

    /*
    public Company getByJoinCode(String joinCode) {

        DynamoDbIndex<Company> index =
                table().index("joinCode-index");

        QueryConditional condition =
                QueryConditional.keyEqualTo(
                        Key.builder()
                           .partitionValue(joinCode)
                           .build()
                );

        return index.query(condition)
                    .stream()
                    .flatMap(p -> p.items().stream())
                    .findFirst()
                    .orElse(null);
    }
    */
}
