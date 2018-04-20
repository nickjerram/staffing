package org.camra.staffing.email;

import java.util.HashSet;
import java.util.Set;

public class ToggleSet<D> extends HashSet<D> {

    public void toggle(D entry) {
        if (contains(entry)) {
            remove(entry);
        } else {
            add(entry);
        }
    }
}
