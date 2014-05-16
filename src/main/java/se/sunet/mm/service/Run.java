package se.sunet.mm.service;

import org.apache.commons.cli.*;
import se.sunet.mm.service.server.Server;

import java.io.File;

public class Run extends Server {

    private static String configFile = "/opt/eduid/etc/mm-service.properties";

    public static void main(String[] args) {
        CommandLineParser parser = new PosixParser();

        Options options = new Options();
        options.addOption("h", "help", false, "This usage information");
        options.addOption("c", "config", true,
                "Path to configuration file, default /opt/eduid/etc/mm-service.properties" );

        try {
            CommandLine commandLine = parser.parse(options, args);

            if (commandLine.hasOption("h")) {
                printUsage(options);
                System.exit(0);
            }

            if (commandLine.hasOption("c")) {
                configFile = commandLine.getOptionValue("c");
                File config = new File(configFile);
                if (!config.exists()) {
                    System.err.println("ERROR: Config file not found!");
                    System.exit(1);
                }
            }

        } catch (ParseException e) {
            System.err.println("Bad command arguments.");
            printUsage(options);
            System.exit(1);
        }
        //Run().start(configFile);
    }

    public static void printUsage(Options options) {
        HelpFormatter help = new HelpFormatter();
        help.setWidth(80);
        help.printHelp("[-c <config>] [OPTION]...", "", options, "");
    }
}
