/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.powsybl.iidm.network.ReactiveLimitsKind;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.TreeMap;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderClassName = "ReactiveCapabilityCurveAttributesBuilder")
@Schema(description = "Reactive capability curve attributes")
public class ReactiveCapabilityCurveAttributes implements ReactiveLimitsAttributes {

    /**
     * <p>Comparator to handle the -0.0 == 0.0 case:</p>
     * <p>According to the JLS: "Positive zero and negative zero compare equal, so the result of the expression 0.0==-0.0 is true".
     * But the {@link Double#compareTo(Double)} method consider -0.0 lower than 0.0. Therefore, using the default
     * Double comparator causes a problem when the lower point's <code>p</code> is equal to 0.0 and the tested <code>p</code>
     * is -0.0.
     * </p>
     * <p>This comparator considers 0.0 and -0.0 as equal.</p>
     * <p>Note: it throws a {@link NullPointerException} when one of the Doubles are null,
     * similarly as the default Double comparator. But in our use case, this cannot happen.</p>
     */
    private static final Comparator<Double> COMPARATOR = (d1, d2) -> d1 - d2 == 0 ? 0 : Double.compare(d1, d2);

    @Schema(description = "Kind of reactive limit")
    private final ReactiveLimitsKind kind = ReactiveLimitsKind.CURVE;

    @Schema(description = "curve points")
    private TreeMap<Double, ReactiveCapabilityCurvePointAttributes> points;

    @Schema(description = "owner description")
    private String ownerDescription;

    /**
     * <p>Specific builder for ReactiveCapabilityCurveAttributes to handle the -0.0 == 0.0 case</p>
     */
    public static class ReactiveCapabilityCurveAttributesBuilder {
        private TreeMap<Double, ReactiveCapabilityCurvePointAttributes> points;

        public ReactiveCapabilityCurveAttributesBuilder points(TreeMap<Double, ReactiveCapabilityCurvePointAttributes> points) {
            if (points == null) {
                this.points = null;
            } else if (points.comparator() == COMPARATOR) {
                this.points = points;
            } else {
                this.points = new TreeMap<>(COMPARATOR);
                this.points.putAll(points);
            }
            return this;
        }

        public ReactiveCapabilityCurveAttributes build() {
            // Some more checks even if the points() method did the work
            if (points != null && points.comparator() != COMPARATOR) {
                TreeMap<Double, ReactiveCapabilityCurvePointAttributes> reordered = new TreeMap<>(COMPARATOR);
                reordered.putAll(points);
                points = reordered;
            }
            return new ReactiveCapabilityCurveAttributes(points, ownerDescription);
        }
    }
}
