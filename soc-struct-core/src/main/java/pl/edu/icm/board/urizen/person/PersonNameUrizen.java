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

package pl.edu.icm.board.urizen.person;

import net.snowyhollows.bento.annotation.WithFactory;
import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.em.socstruct.component.NameTag;
import pl.edu.icm.em.socstruct.component.Household;
import pl.edu.icm.em.socstruct.component.Person;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.bin.Bin;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.selector.Selector;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.util.Status;

import static pl.edu.icm.trurl.ecs.util.EntityIterator.select;

public class PersonNameUrizen {
    private final EngineIo engineIo;
    private final NamesSource namesSource;
    private final RandomGenerator random;
    private final Indexes selectors;

    @WithFactory
    public PersonNameUrizen(EngineIo engineIo,
                            NamesSource namesSource,
                            RandomProvider randomProvider,
                            Indexes selectors) {
        this.engineIo = engineIo;
        this.namesSource = namesSource;
        this.selectors = selectors;
        engineIo.require(NameTag.class, Household.class, Person.class);
        this.random = randomProvider.getRandomGenerator(PersonNameUrizen.class);
    }

    public void giveNames() {
        Status stats = Status.of("Giving names", 1_000_000);
        NamePools names = namesSource.load();

        engineIo.getEngine().execute(select(createHouseholdSelector()).forEach(entity -> {
            Bin<String> lastNameBin = names.surnames.sample(random.nextDouble());
            Household household = entity.get(Household.class);
            for (Entity memberEntity : household.getMembers()) {
                if (lastNameBin.getCount() <= 0 || random.nextDouble() < 0.15) {
                    lastNameBin = names.surnames.sample(random.nextDouble());
                }
                String lastName = lastNameBin.pick();
                Person person = memberEntity.get(Person.class);
                String firstName = person.getSex() ==
                        Person.Sex.F
                        ? names.femaleNames.sample(random.nextDouble()).pick()
                        : names.maleNames.sample(random.nextDouble()).pick();
                if ((lastName.endsWith("SKI") || lastName.endsWith("CKI")) && person.getSex() == Person.Sex.F) {
                    lastName = lastName.substring(0, lastName.length() - 1) + "A";
                }
                NameTag nameTag = new NameTag();
                nameTag.setName(firstName + " " + lastName);
                memberEntity.add(nameTag);
                stats.tick();
            }
        }));

        stats.done();
    }

    private Selector createHouseholdSelector() {
        return selectors.filtered(selectors.allEntities(10_000), selectors.hasComponents(Household.class));
    }
}
