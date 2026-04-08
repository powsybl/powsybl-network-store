/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

/**
 * Attribute filters control which subset of resource data is serialized.
 * <p>
 * The default (that is represented by null) corresponds to the "primary" view:
 * all standard fields are included, but extra fields (limits, in the future
 * potentially more like extensions) are not included.
 * <p>
 * Filters are either subtractive (sending less than the primary view,
 * e.g. SV) or additive (sending more than the primary view, e.g. LIMITS).
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum AttributeFilter {

    // Note: all the following numeric priority values are not persisted and can be
    // changed freely to fit new requirements (e.g. new graph of data subsets/supersets).

    // Negative values are subsets of the primary.
    SV(-1),

    // here between the negatives and the positives, the primary view (see the
    // constant PRIMARY_PRIORITY = 0)
    // Not sure if it is a good design to use null as the marker for the PRIMARY set
    // or is better to have an actual enum member PRIMARY(0) ? Or both ?

    // Positive values are supersets of the primary.
    LIMITS(1),
    FULL(2);

    // null and priority as constants to be able to convey meaning and
    // differentiate in the source code from other nulls and 0s. Conceptually
    // null/0 replace a PRIMARY enum member which would be declared above but
    // is not declared.  Not sure if it is a good idea..
    public static final AttributeFilter PRIMARY_AS_NULL = null;
    private static final int PRIMARY_PRIORITY = 0;

    private final int priority;

    AttributeFilter(int priority) {
        this.priority = priority;
    }

    // Returns a display label for the given view class (e.g. " OnlySv", " WithLimits"), or empty string for Primary.
    public static String getLabelFromView(Class<?> viewClass) {
        if (viewClass == JsonViews.Primary.class) {
            return "";
        }
        return " " + (viewClass != JsonViews.FULL_AS_NULL ? viewClass.getSimpleName() : "FULL");
    }

    // Returns a URL path suffix for subset filters (e.g. "/sv"), or empty string otherwise.
    // For sending limits to the server basic url and dto are used as they are present in the attributes
    public static String getUrlSuffix(AttributeFilter filter) {
        return getEffectivePriority(filter) < PRIMARY_PRIORITY ?
            "/" + filter.name().toLowerCase() : "";
    }

    // Returns the priority for the given filter, where null means the primary/standard view.
    private static int getEffectivePriority(AttributeFilter filter) {
        return filter == PRIMARY_AS_NULL ? PRIMARY_PRIORITY : filter.priority;
    }

    /** Returns the covering filter (potentially a new broader one) of two filters.
     *
     * When two different filters have the same priority, covering promotes to the
     * next broader category.
     */
    public static AttributeFilter covering(AttributeFilter prev, AttributeFilter next) {
        return covering(prev, getEffectivePriority(prev), next, getEffectivePriority(next), PRIMARY_AS_NULL, FULL);
    }

    // Package-private for testability: allows tests to pass explicit priorities
    // to exercise promotion logic without needing peer enum members.
    // Generic so that production code calls it with AttributeFilter (no cast)
    // while tests call it with Object. When we implement promotion results
    // other than PRIMARY or FULL, we will have to come here to change
    // the promotion returns and update the matching test.
    // Feel free to remove when new enum members allow to test everything without it.
    static <T> T covering(T prev, int prevPriority, T next, int nextPriority, T primary, T full) {
        if (prev != next && prevPriority == nextPriority) {
            if (prevPriority == PRIMARY_PRIORITY) {
                throw new IllegalStateException("Primary should have no peers");
            }
            if (prevPriority == FULL.priority) {
                throw new IllegalStateException("FULL should have no peers");
            }
            // Different filters at the same level: promote to the next broader category
            return prevPriority < PRIMARY_PRIORITY ? primary : full;
        }
        return prevPriority >= nextPriority ? prev : next;
    }

    public static Class<?> getViewClass(AttributeFilter filter) {
        if (filter == PRIMARY_AS_NULL) {
            return JsonViews.Primary.class;
        }
        return switch (filter) {
            case SV -> JsonViews.OnlySv.class;
            case LIMITS -> JsonViews.WithLimits.class;
            case FULL -> JsonViews.FULL_AS_NULL;
        };
    }

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
    public static final class JsonViews {

        private JsonViews() { }

        public interface OnlySv { }

        public interface Primary extends OnlySv { }

        public interface WithLimits extends Primary { }

        // null as a constant to be able to convey meaning and differentiate in the
        // source code from other nulls. Not sure if it is a good idea..
        // This is a behavior of jackson.
        public static final Class<?> FULL_AS_NULL = null;
    }
}
