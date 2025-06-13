import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class PostgresInsertGenerator {

    // Configuration - change these values as needed
    private static final String CSV_FILE_PATH = "Ontada_OIDS_FOR_US_CORE_AND_FHIR.csv";
    private static final String OUTPUT_FILE_PATH = "oid_master_inserts.sql";
    private static final String TABLE_NAME = "\"code-bridge\".oid_master";
    private static final int OID_COLUMN_INDEX = 4; // Index of the OID column in CSV (0-based)

    // Random generator for creating random values
    private static final Random random = new Random();

    public static void main(String[] args) {
        try {
            // Read CSV file
            List<String[]> csvData = readCsv(CSV_FILE_PATH);
            System.out.println("CSV file read successfully with " + csvData.size() + " rows.");

            // Filter out duplicate OIDs
            List<String[]> uniqueOidData = filterDuplicateOids(csvData);
            System.out.println("Filtered to " + uniqueOidData.size() + " rows with unique OIDs.");
            System.out.println("Removed " + (csvData.size() - uniqueOidData.size()) + " duplicate OIDs.");

            // Generate SQL INSERT statements
            String sqlInserts = generateInsertStatements(uniqueOidData);

            // Write to output file
            writeToFile(OUTPUT_FILE_PATH, sqlInserts);
            System.out.println("INSERT statements generated successfully and saved to " + OUTPUT_FILE_PATH);

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<String[]> readCsv(String filePath) throws IOException {
        List<String[]> data = new ArrayList<>();
        String line;
        boolean isFirstLine = true;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            while ((line = br.readLine()) != null) {
                // Skip header row
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // Handle simple CSV parsing (assumes no quotes or commas in fields)
                String[] fields = line.split(",");
                data.add(fields);
            }
        }

        return data;
    }

    private static List<String[]> filterDuplicateOids(List<String[]> csvData) {
        List<String[]> uniqueData = new ArrayList<>();
        Set<String> seenOids = new HashSet<>();

        // Track which OIDs are duplicates for reporting
        Set<String> duplicateOids = new HashSet<>();

        for (String[] row : csvData) {
            String oid = row[OID_COLUMN_INDEX];

            if (!seenOids.contains(oid)) {
                // This is the first time we're seeing this OID
                seenOids.add(oid);
                uniqueData.add(row);
            } else {
                // This is a duplicate OID
                duplicateOids.add(oid);
            }
        }

        // Print out the duplicate OIDs
        System.out.println("Duplicate OIDs found (" + duplicateOids.size() + " unique OIDs with duplicates):");
        for (String oid : duplicateOids) {
            System.out.println("  - " + oid);
        }

        return uniqueData;
    }

    private static String generateInsertStatements(List<String[]> csvData) {
        StringBuilder sql = new StringBuilder();

        // Start transaction
        sql.append("-- PostgreSQL INSERT statements for ").append(TABLE_NAME).append("\n");
        sql.append("-- Generated on: ").append(new SimpleDateFormat("yyyy-MM-dd").format(new Date())).append("\n");
        sql.append("-- Total rows: ").append(csvData.size()).append(" (duplicate OIDs removed)\n\n");
        sql.append("BEGIN;\n\n");

        // Generate INSERT statements for each row
        for (int i = 0; i < csvData.size(); i++) {
            String[] row = csvData.get(i);
            int id = i + 1; // Incremental ID starting from 1

            // Generate random values
            String backendCacheKey = "cache_" + UUID.randomUUID().toString().substring(0, 8);
            boolean backendIsCached = random.nextBoolean();
            String backendRedisExpiry = randomFutureTimestamp();
            boolean consumerIsCached = random.nextBoolean();
            String consumerRedisExpiry = randomFutureTimestamp();
            int consumerCacheHits = random.nextInt(1000);
            String status = randomStatus();
            String lastStatusUpdate = randomPastTimestamp();
            String createdAt = randomPastTimestamp();
            String updatedAt = randomPastTimestamp();

            // 70% chance of having a last_api_call
            boolean hasLastApiCall = random.nextDouble() > 0.3;
            String lastApiCall = hasLastApiCall ? randomPastTimestamp() : "NULL";
            int apiCallCount = hasLastApiCall ? random.nextInt(500) : 0;

            // Build INSERT statement
            sql.append("INSERT INTO ").append(TABLE_NAME).append(" (\n");
            sql.append("  id, oid, backend_cache_key, backend_is_cached, backend_redis_cache_expiry, \n");
            sql.append("  consumer_is_cached, consumer_redis_cache_expiry, consumer_cache_hits, \n");
            sql.append("  hli_api_config_id, is_active, status, last_status_update, created_at, \n");
            sql.append("  updated_at, last_api_call, api_call_count, code_group_content_set, \n");
            sql.append("  code_group_content_set_version, code_sub_type, fhir_identifier, hl7_uri, \n");
            sql.append("  code_group_name, description, code_group_revision_name, revision_start, \n");
            sql.append("  revision_end\n");
            sql.append(") VALUES (\n");

            // Add values from CSV and random generators
            sql.append("  ").append(id).append(", -- id\n");
            sql.append("  '").append(escapeSQL(row[OID_COLUMN_INDEX])).append("', -- oid\n");
            sql.append("  '").append(backendCacheKey).append("', -- backend_cache_key\n");
            sql.append("  ").append(backendIsCached).append(", -- backend_is_cached\n");
            sql.append("  TIMESTAMP '").append(backendRedisExpiry).append("', -- backend_redis_cache_expiry\n");
            sql.append("  ").append(consumerIsCached).append(", -- consumer_is_cached\n");
            sql.append("  TIMESTAMP '").append(consumerRedisExpiry).append("', -- consumer_redis_cache_expiry\n");
            sql.append("  ").append(consumerCacheHits).append(", -- consumer_cache_hits\n");
            sql.append("  1, -- hli_api_config_id\n");
            sql.append("  TRUE, -- is_active\n");
            sql.append("  '").append(status).append("', -- status\n");
            sql.append("  TIMESTAMP '").append(lastStatusUpdate).append("', -- last_status_update\n");
            sql.append("  TIMESTAMP '").append(createdAt).append("', -- created_at\n");
            sql.append("  TIMESTAMP '").append(updatedAt).append("', -- updated_at\n");
            sql.append("  ").append(hasLastApiCall ? "TIMESTAMP '" + lastApiCall + "'" : "NULL")
                    .append(", -- last_api_call\n");
            sql.append("  ").append(apiCallCount).append(", -- api_call_count\n");
            sql.append("  '").append(escapeSQL(row[0])).append("', -- code_group_content_set\n");
            sql.append("  '").append(escapeSQL(row[1])).append("', -- code_group_content_set_version\n");
            sql.append("  '").append(escapeSQL(row[2])).append("', -- code_sub_type\n");
            sql.append("  '").append(escapeSQL(row[3])).append("', -- fhir_identifier\n");
            sql.append("  '").append(escapeSQL(row[5])).append("', -- hl7_uri\n");
            sql.append("  '").append(escapeSQL(row[6])).append("', -- code_group_name\n");
            sql.append("  '").append(escapeSQL(row[7])).append("', -- description\n");
            sql.append("  '").append(escapeSQL(row[8])).append("', -- code_group_revision_name\n");
            sql.append("  '").append(escapeSQL(row[9])).append("', -- revision_start\n");

            // Handle possible NULL in revision_end
            if (row.length > 10 && row[10] != null && !row[10].isEmpty()) {
                sql.append("  '").append(escapeSQL(row[10])).append("' -- revision_end\n");
            } else {
                sql.append("  NULL -- revision_end\n");
            }

            sql.append(");\n\n");
        }

        // End transaction
        sql.append("COMMIT;\n");

        return sql.toString();
    }

    private static void writeToFile(String filePath, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        }
    }

    // Helper method to escape SQL strings
    private static String escapeSQL(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("'", "''");
    }

    // Generate a random status value
    private static String randomStatus() {
        String[] statuses = { "active", "pending", "inactive" };
        return statuses[random.nextInt(statuses.length)];
    }

    // Generate a random timestamp in the past (within last year)
    private static String randomPastTimestamp() {
        long now = System.currentTimeMillis();
        long oneYearAgo = now - (365L * 24 * 60 * 60 * 1000);
        long randomTime = ThreadLocalRandom.current().nextLong(oneYearAgo, now);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(randomTime));
    }

    // Generate a random timestamp in the future (within next 30 days)
    private static String randomFutureTimestamp() {
        long now = System.currentTimeMillis();
        long thirtyDaysLater = now + (30L * 24 * 60 * 60 * 1000);
        long randomTime = ThreadLocalRandom.current().nextLong(now, thirtyDaysLater);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(randomTime));
    }
}