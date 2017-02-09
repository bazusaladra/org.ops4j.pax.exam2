/*
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
 */
package org.ops4j.pax.exam.sample.karaf;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;

import org.apache.karaf.features.FeaturesService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.sample8.ds.Calculator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.inject.Inject;

@RunWith(PaxExam.class)
public class CalculatorITest {

  private static Logger LOG = LoggerFactory.getLogger(CalculatorITest.class);

  @Inject
  protected Calculator calculator;

  @Inject
  protected FeaturesService featuresService;

  @Inject
  protected BundleContext bundleContext;

  @Configuration
  public Option[] config() {
    MavenArtifactUrlReference karafUrl = maven()
        .groupId("com.ectsp")
        .artifactId("ectsp-karaf")
        .version("1.0")
        .type("zip");
    MavenArtifactUrlReference karafOriginalUrl = maven()
        .groupId("org.apache.karaf")
        .artifactId("apache-karaf")
        .version("4.0.8")
        .type("zip");

    return new Option[]{
        // KarafDistributionOption.debugConfiguration("5005", true),
        karafDistributionConfiguration()
            .frameworkUrl(karafOriginalUrl)
            .unpackDirectory(new File("target", "exam"))
            .useDeployFolder(false),
        keepRuntimeFolder(),
        configureConsole().ignoreLocalConsole(),
        features(maven()
            .groupId("org.apache.karaf.features")
            .artifactId("standard")
            .version(karafVersion())
            .classifier("features")
            .type("xml"), "scr"),
        features(maven()
                .groupId("com.ectsp")
                .artifactId("ectsp-karaf-feature")
                .version("1.0.0-SNAPSHOT")
                .classifier("features")
                .type("xml")
//                ,"ectsp-pax-jdbc-pool-aries", "ectsp-pax-jdbc-pool-dbcp2", "quartz", "ectsp-activemq-client", "ectsp-karaf-feature"
        ),
        features(maven()
            .groupId("com.ectsp")
            .artifactId("ectsp-extensions-karaf-feature")
            .version("1.0.0-SNAPSHOT")
            .classifier("features")
            .type("xml")),
        features(maven()
            .groupId("com.ectsp")
            .artifactId("ectsp-spring-osgi-feature")
            .version("1.0.0-SNAPSHOT")
            .classifier("features")
            .type("xml")),
        mavenBundle()
            .groupId("org.ops4j.pax.exam.samples")
            .artifactId("pax-exam-sample8-ds")
            .versionAsInProject().start(),
    };
  }

  public static String karafVersion() {
    ConfigurationManager cm = new ConfigurationManager();
    String karafVersion = cm.getProperty("pax.exam.karaf.version", "4.0.8");
    return karafVersion;
  }


  @Test
  public void testAdd() throws Exception {

    LOG.info("{}", featuresService.listFeatures());
    LOG.info("{}", Arrays.asList(bundleContext.getBundles()));

    featuresService.addRepository(URI.create("mvn:org.ops4j.pax.jdbc/pax-jdbc-features/0.9.0/xml/features"));
    featuresService.addRepository(URI.create("mvn:org.drools/drools-karaf-features/6.3.0.Final/xml/features"));
    featuresService.addRepository(URI.create("mvn:org.apache.activemq/activemq-karaf/5.13.4/xml/features"));
    featuresService.addRepository(URI.create("mvn:com.ectsp/ectsp-karaf-feature/1.0.0-SNAPSHOT/xml/features"));
    featuresService.addRepository(URI.create("mvn:com.ectsp/ectsp-extensions-karaf-feature/1.0.0-SNAPSHOT/xml/features"));
    featuresService.addRepository(URI.create("mvn:com.ectsp/ectsp-spring-osgi-feature/1.0.0-SNAPSHOT/xml/features"));

    System.out.print(Arrays.asList(featuresService.listRepositories()).stream().map(s->s.getURI()).collect(Collectors.toList()));

//    featuresService.installFeature("ectsp-pax-jdbc-pool-aries");
//    featuresService.installFeature("ectsp-pax-jdbc-pool-dbcp2");
//    featuresService.installFeature("quartz");
//    featuresService.installFeature("ectsp-activemq-client");
    featuresService.installFeature("ectsp-karaf-feature");
    featuresService.installFeature("ectsp-extensions-karaf-feature");
    featuresService.installFeature("ectsp-spring-osgi-feature");

    System.out.print(Arrays.asList(featuresService.listFeatures()));
    System.out.print(Arrays.asList(bundleContext.getBundles()));

    int result = calculator.add(1, 2);
    LOG.info("Result of add was {}", result);
    Assert.assertEquals(3, result);
  }

}
