/*
 * Copyright 2019 Jonathan Chang, Chun-yien <ccy@musicapoetica.org>.
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
 */
package tech.metacontext.ocnhfa.cf_composer.model;

import java.util.Random;
import tech.metacontext.ocnhfa.antsomg.impl.StandardGraph.FractionMode;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public final class Parameters {

    public static FractionMode DEFAULT_FRACTION_MODE = FractionMode.Coefficient;

    public static int DEFAULT_THREAD_NUMBER = 1000;
    public static int DEFAULT_COMPOSER_NUMBER = 5;
    public static int CF_LENGTH_LOWER = 7;
    public static int CF_LENGTH_HIGHER = 15;
    public static int CF_RANGE_LOWER = 4;
    public static int CF_RANGE_HIGHER = 8;

    public static double DEFAULT_COST = 1.0;
    public static double DOMINANT_ATTRACTION_FACTOR = 10.0;
    public static int DOMINANT_COUNT = 3;
    public static int DEFAULT_TARGET_SIZE = 20;

    public static double X_PHEROMONE_EVAPORATE_RATE = 0.1;
    public static double X_PHEROMONE_DEPOSIT_AMOUNT = 1.0;
    public static double X_ALPHA = 2.0, X_BETA = 1.0;
    public static double X_EXPLORE_CHANCE = 0.1;

    public static double Y_PHEROMONE_EVAPORATE_RATE = 0.1;
    public static double Y_PHEROMONE_DEPOSIT_AMOUNT = 1.0;
    public static double Y_ALPHA = 1.0, Y_BETA = 1.0;
    public static double Y_EXPLORE_CHANCE = 0.2;

    public static Random RANDOM = new Random();
}
