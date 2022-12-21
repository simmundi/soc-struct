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

import net.snowyhollows.bento.config.Configurer;
import net.snowyhollows.bento.config.DefaultWorkDir;
import net.snowyhollows.bento.Bento;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

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

    public static Configurer configurer(String[] args) throws IOException {
        CommandLine line = parseCommandLine(args);
        String rootPath = List.of(
                        ofNullable(line.getOptionValue(homeOption)),
                        ofNullable(System.getenv("EM_HOME")),
                        of("."))
                .stream()
                .filter(Optional::isPresent).findFirst().get().get();
        if (line.hasOption(listProperties)) {
            emBentoInspector = new EmBentoInspector();
            Bento.inspector = emBentoInspector;
        }
        return new EmConfigurer(new DefaultWorkDir(new File(rootPath)), configurer -> postConfigure(line, configurer))
                .loadConfigDir("input/config")
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
                    configurer.loadConfigFile(file);
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
                System.out.println();
                System.out.println("Effective properties:");
                System.out.println("=====================");
                emBentoInspector.values().forEach(entry -> {
                    System.out.println(String.format("%s = %s", entry.getKey(), entry.getValue()));
                });
                System.out.println();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static CommandLine parseCommandLine(String[] args) {
        Options options = new Options()
                .addOption(parameterOption = Option.builder("P")
                        .desc("sets/overrides a single property, e.g. -PsqlBatchSize=9999")
                        .hasArgs()
                        .valueSeparator('=').build())
                .addOption(fileOption = Option.builder("F")
                        .desc("reads a single property file, e.g. -Finput/config/healthcare/healthcare.properties")
                        .hasArg()
                        .build())
                .addOption(homeOption = Option.builder("home")
                        .desc("sets home directory, e.g. -home ../pdyn-stack")
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
