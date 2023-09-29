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

package pl.edu.icm.board.squaronia;

import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.bin.BinPool;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.util.Status;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public class SquaroniaUrizen {
    private int familySize;
    private int populationSize;
    private int borderLength;
    private int numberOfWomen;
    private int numberOfMen;

    private final EngineIo engineIo;

    private BinPool<AgeRange> ageRangeBinPool = null;
    private BinPool<Person.Sex> sexPoll = null;

    private final RandomGenerator random;


    public SquaroniaUrizen(EngineIo engineIo,
                           int familySize,
                           int populationSize,
                           int borderLength,
                           float percentOfWomen,
                           RandomProvider randomProvider) {
        this.engineIo = engineIo;
        this.withPopulationSize(populationSize);
        this.withFamilySize(familySize);
        this.withPercentOfWomen(percentOfWomen);
        this.withBorderLength(borderLength);
        this.random = randomProvider.getRandomGenerator(SquaroniaUrizen.class);
    }

    /**
     * Add age group share to age pool e.g.
     * squaroniaUrizen.withAgeGroupShare(AgeRange.AGE_0_4,1).withAgeGroupShare(AgeRange.AGE_15_19,3)
     * will effect in 1/4 citizens aged 0-4 and 3/4 aged 15-19
     * @param ageRange which range
     * @param shares how much shares
     * @return self (SquaroniaUrizen)
     */
    public SquaroniaUrizen withAgeGroupShare(AgeRange ageRange, int shares){
        checkArgument(shares > 0, "shares == " + shares + " <= 0");
        if(this.ageRangeBinPool == null){
            this.ageRangeBinPool = new BinPool<>();
        }
        this.ageRangeBinPool.add(ageRange, shares);
        return this;
    }

    /**
     * Define sex structure in Squaronia
     * @param percentOfWomen percent of women in Squaronia
     * @return self (SquaroniaUrizen)
     * @throws IllegalArgumentException if percentOfWomen not in [0, 100]
     */
    public SquaroniaUrizen withPercentOfWomen(float percentOfWomen) {
        checkArgument(percentOfWomen >= 0 && percentOfWomen <= 100, "percentOfWomen == " + percentOfWomen + " not in (0,100)");
        this.numberOfWomen = (int)(this.populationSize * percentOfWomen / 100);
        this.numberOfMen = this.populationSize - this.numberOfWomen;
        return this;
    }

    /**
     * Define Squaronia family size
     * @param familySize number of people in one houshold
     * @return self (SquaroniaUrizen)
     * @throws IllegalArgumentException if familySize less or equal zero
     */
    public SquaroniaUrizen withFamilySize(int familySize) {
        checkArgument(familySize > 0, "familySize == " + familySize + " <= 0");
        this.familySize = familySize;
        return this;
    }

    /**
     * Define Squaronia population size
     * @param populationSize number of Squaronia citizens
     * @return self (SquaroniaUrizen)
     * @throws IllegalArgumentException if populationSize less than 0
     */
    public SquaroniaUrizen withPopulationSize(int populationSize) {
        checkArgument(populationSize >= 0, "populationSize == " + populationSize + " < 0");
        this.populationSize = populationSize;
        return this;
    }

    /**
     * Define side length of Squaronia
     * @param borderLength side length (in km)
     * @return self (SquaroniaUrizen)
     * @throws IllegalArgumentException if borderLength less or equal 0
     */
    public SquaroniaUrizen withBorderLength(int borderLength) {
        checkArgument(borderLength > 0, "borderLength == " + borderLength + " <= 0");
        this.borderLength = borderLength;
        return this;
    }

    /**
     * Builds Squaronia
     */
    public void build() {
        if (ageRangeBinPool == null){
            generateDefaultAgePool();
        }
        if (numberOfWomen == -1){
          numberOfWomen = populationSize / 2;
          numberOfMen = populationSize - numberOfWomen;
        }
        generateSexPoll();
        engineIo.require(Household.class, Location.class, Person.class);
        createHouseholdsWithCitizens();
    }

    private void generateSexPoll() {
        this.sexPoll = new BinPool<>();
        this.sexPoll.add(Person.Sex.M,this.numberOfMen);
        this.sexPoll.add(Person.Sex.K,this.numberOfWomen);
    }

    private void generateDefaultAgePool() {
        ageRangeBinPool = new BinPool<>();
        ageRangeBinPool.add(AgeRange.AGE_0_100,populationSize);
    }

    private void createHouseholdsWithCitizens() {
        var engine = engineIo.getEngine();
        engine.execute(sessionFactory-> {
            Session session = sessionFactory.create();
            Status statshaushold = Status.of("Building Squaronia's households",10000);
            int tempPop = this.populationSize;
            for (int i = 0; i < (float)this.populationSize / this.familySize; i++){
                int posN = i % this.borderLength;
                int posE = (i / this.borderLength) % this.borderLength;
                Entity house = createEmptyHousehold(session, posN, posE);
                List<Entity> housemates = house.get(Household.class).getMembers();

                for (int j = 0; j < this.familySize && tempPop > 0; j++, tempPop--){
                    createCitizenInHousehold(session, housemates);
                }
                statshaushold.tick();
            }
            statshaushold.done();
            session.close();

        });
    }

    private Entity createEmptyHousehold(Session session, int posN, int posE) {
        Entity house = session.createEntity();
        house.add(new Household());
        var location = house.add(new Location());
        location.setN(posN);
        location.setE(posE);
        return house;
    }

    private void createCitizenInHousehold(Session session, List<Entity> housemates) {
        var sex = sexPoll.sample(random.nextDouble()).pick();
        var age = ageRangeBinPool.sample(random.nextDouble()).pick().getValue(random.nextDouble());
        Entity cit = session.createEntity();
        Person person = cit.add(new Person());
        person.setAge(age);
        person.setSex(sex);
        housemates.add(cit);
    }
}

