package dynamodb.pagination.poc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.configuration.picocli.PicocliRunner;

import io.micronaut.context.ApplicationContext;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.mysqlclient.MySQLPool;
import io.vertx.reactivex.sqlclient.Row;
import io.vertx.reactivex.sqlclient.RowIterator;
import io.vertx.reactivex.sqlclient.Tuple;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Command(name = "dynamodb-pagination-poc", description = "...",
        mixinStandardHelpOptions = true)
public class DynamodbPaginationPocCommand implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(DynamodbPaginationPocCommand.class);

    public static void main(String[] args) throws Exception {
        PicocliRunner.run(DynamodbPaginationPocCommand.class, args);
    }

    private MySQLPool pool;

    public DynamodbPaginationPocCommand() {
        pool = ApplicationContext.run().createBean(MySQLPool.class);
    }

    public void run() {
        //popular();
        ler();
    }

    public void ler() {

        String cliente = "AAAAAAAAAA";

        List<JsonObject> resultado = new ArrayList<>();

        String query = "select JSON_SET(s.payload, '$.periodicidade', p.valor) payload " +
                        " from saldos s inner join periodicidades p " +
                        " on s.periodicidadeId = p.id " +
                        " where cliente = ?";

        pool.preparedQuery(query)
                .rxExecute(Tuple.of(cliente))
                .map(rowSet -> {
                    RowIterator<Row> iterator = rowSet.iterator();
                    while (iterator.hasNext()) {
                        Row row = iterator.next();
                        JsonObject payload = (JsonObject)row.getValue("payload");
                        resultado.add(payload);
                    }

                    return "";

                }).blockingGet();


        resultado.forEach(obj -> {
            logger.info(obj.encodePrettily());
        });
    }

    @SneakyThrows
    public void popular() {

        String arquivoTesteCaminho = getClass().getClassLoader().getResource("Saldos").getPath();
        FileInputStream fis = new FileInputStream(arquivoTesteCaminho);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader reader = new BufferedReader(isr);

        String linha = null;

        while ((linha = reader.readLine()) != null) {

            String cliente = linha.substring(0, 10).trim();
            String data = linha.substring(11, 18).trim();

            Cliente c = new Cliente();
            c.setCliente(cliente);
            c.setData(data);
            c.setObs("N/A");

            ObjectMapper objectMapper = new ObjectMapper();
            String clienteJson = objectMapper.writeValueAsString(c);

            pool.preparedQuery("INSERT INTO saldos values(default, ?, ?, NULL)")
                    .execute(Tuple.of(c.getCliente(), clienteJson), ar -> {
                        if (ar.succeeded()) {
                            logger.info(String.format(" %s salvo ", clienteJson));
                        } else {
                            logger.error("oh, shit!", ar.cause());
                        }
                    });
        }
    }
}
