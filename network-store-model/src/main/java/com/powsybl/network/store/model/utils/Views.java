/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model.utils;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 * @author Jon Harper <jon.harper at rte-france.com>
 *
 * Classes to use with Jackson's @JsonView to serialize subsets
 * of our resources.
 *
 * In our use case, the primary view is in the middle of the hierarchy:
 * - "OnlyX" views are subsets of the primary view (e.g. OnlySv)
 * - primary view
 * - "WithX" views are supersets of the primary view (e.g. WithLimits)
 * - no (or null) view always have the full data like in jackson
 *
 * Current design:
 * - for classes without subsets/supersets, nothing needs to be done.
 * - For classes with supersets, annotate the fields of the superset with
 *   @JsonView(Superset.class).  the superset fields will be excluded from
 *   the primary view and included in their superset view.
 * - For classes with subsets, annotate the fields of the subset with
 *   @JsonView(Subset.class) and additionally make sure that the class itself is
 *   with @JsonView(Primary.class) (not necessary for classes extending our base
 *   attributes class which is already annotated). The subset fields will
 *   be included in the their subset view and excluded from the primary view.
 *
 * Note: We don't need the "exhaustive whitelist" mode of
 * DEFAULT_VIEW_INCLUSION=false which is AFAIK mostly intended to avoid
 * exposing sensitive data (you have to annotate every class in the serialized
 * graph, kind of in a flat way). Instead, by always using
 * DEFAULT_VIEW_INCLUSION=true, the inclusion behaves "hierarchically": data is
 * included as soon as the field in the parent POJO has the annotation. In
 * other words, we don't model our primary view by having fields in the
 * jackson's default view and changing the value of DEFAULT_VIEW_INCLUSION
 * (false for subset views, true for superset views). Besides, always using
 * DEFAULT_VIEW_INCLUSION=true removes the need for using two separate object
 * mappers as DEFAULT_VIEW_INCLUSION is an objectmapper static property that
 * can't be toggled dynamically.
 *
 * Future ideas: The current code use a simple hierarchy which allows the use
 * cases with want, but doesn't allow arbitrary combinations. If we ever
 * need arbitrary combinations, we could implement more verbose annotations
 * where we don't use inheritance at the field annotation level, only use
 * inheritance for the serialization active view.
 */
public final class Views {

    private Views() { }

    public interface OnlySv { }

    public interface Primary extends OnlySv { }

    public interface WithLimits extends Primary { }
}
