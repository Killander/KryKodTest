package se.kry.codetest.migrate;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.sql.ResultSet;
import se.kry.codetest.DBConnector;

public class DBMigration {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DBConnector connector = new DBConnector(vertx);

        //Drop Table
        Future<ResultSet> completed_db_cleanup = connector.query(
                "drop table service ")
                .setHandler(done -> {
                    if (done.succeeded()) {
                        System.out.println("completed db cleanup");
                    } else {
                        done.cause().printStackTrace();
                    }
                    vertx.close(shutdown -> {
                        System.exit(0);
                    });
                });

        //Create new table
        completed_db_cleanup.setHandler(waitForQueryToComplete -> {
            if (waitForQueryToComplete.succeeded()) {
                connector.query(
                        "create table if not exists service " +
                                "(url varchar(128) not null, " +
                                "name varchar(128) not null, " +
                                "response varchar(128) not null, " +
                                "addedbyuser varchar(128) not null, " +
                                "added datetime not null)")
                        .setHandler(done -> {
                            if (done.succeeded()) {
                                System.out.println("completed db migrations");
                            } else {
                                done.cause().printStackTrace();
                            }
                            vertx.close(shutdown -> {
                                System.exit(0);
                            });
                        });
            }

        });


    }
}
