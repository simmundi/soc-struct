/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package pl.edu.icm.em.common;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.config.Configurer;
import net.snowyhollows.bento.config.DefaultWorkDir;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

public class EmConfig {
    private static Option parameterOption;
    private static Option fileOption;
    private static Option dirOption;
    private static Option homeOption;
    private static Option listProperties;

    private static EmBentoInspector emBentoInspector;

    private EmConfig() {
    }

    public static EmConfigurer debugConfigurer(String[] args) throws IOException {
        String[] newArgs = new String[args.length + 1];
        System.arraycopy(args, 0, newArgs, 0, args.length);
        newArgs[args.length] = "--list-properties";
        return configurer(newArgs);
    }

    public static EmConfigurer configurer(String[] args) throws IOException {
        CommandLine line = parseCommandLine(args);
        String rootPath = Stream.of(
                        ofNullable(line.getOptionValue(homeOption)),
                        ofNullable(System.getenv("EM_HOME")),
                        of("."))
                .filter(Optional::isPresent).findFirst().get().orElseThrow();
        if (line.hasOption(listProperties)) {
            emBentoInspector = new EmBentoInspector();
            Bento.inspector = emBentoInspector;
        }
        File root = new File(rootPath);
        return new EmConfigurer(new DefaultWorkDir(root), root, configurer -> postConfigure(line, configurer))
                .setParam("rootPath", rootPath);
    }

    private static void postConfigure(CommandLine line, EmConfigurer configurer) {
        try {
            if (line.hasOption(dirOption)) {
                for (String dir : line.getOptionValues(dirOption)) {
                    configurer.loadConfigDir(dir);
                }
            }
            if (line.hasOption(fileOption)) {
                for (String file : line.getOptionValues(fileOption)) {
                    configurer.loadHoconFile(file);
                }
            }
            if (line.hasOption(parameterOption)) {
                Properties params = line.getOptionProperties(parameterOption);
                for (Object o : params.keySet()) {
                    String paramName = o.toString();
                    String paramValue = params.getProperty(paramName);
                    configurer.setParam(paramName, paramValue);
                }
            }
            if (line.hasOption(listProperties)) {
                debugProperties();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void debugProperties() {
        System.out.println();
        System.out.println("Effective properties:");
        System.out.println("=====================");
        emBentoInspector.values().forEach(entry ->
            System.out.printf("%s = %s%n", entry.getKey(), entry.getValue())
        );
        System.out.println();
    }

    private static CommandLine parseCommandLine(String[] args) {
        Options options = new Options()
                .addOption(parameterOption = Option.builder("P")
                        .desc("sets/overrides a single property, e.g. -PsqlBatchSize=9999")
                        .hasArgs()
                        .valueSeparator('=').build())
                .addOption(fileOption = Option.builder("F")
                        .desc("reads a single config file, e.g. -Finput/config/healthcare/healthcare.conf")
                        .hasArg()
                        .build())
                .addOption(homeOption = Option.builder()
                        .longOpt("home")
                        .argName("dir")
                        .desc("sets home directory, e.g. --home=../pdyn-stack")
                        .hasArg()
                        .build())
                .addOption(listProperties = Option.builder("lp")
                        .longOpt("list-properties")
                        .desc("lists all the properties")
                        .build())
                .addOption(dirOption = Option.builder("D")
                        .desc("read all property files from a directory (non recursive), e.g. -Dinput/config/healthcare")
                        .hasArg()
                        .build());
        try {
            CommandLineParser parser = new DefaultParser();
            return parser.parse(options, args);
        } catch (ParseException exp) {
            System.out.println(exp.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("em", options);
            System.exit(1);
            throw new IllegalStateException("System.exit should never return");
        }
    }

    public static Bento create(String[] args) throws IOException {
        return configurer(args).getConfig();
    }

    public static Bento create(String[] args, String additionalConfigPath)
            throws IOException {
        return configurer(args).loadConfigDir(additionalConfigPath).getConfig();
    }
}
