
package com.raselahomed467.advancedmedia3;

import android.content.Context;

import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.Form;

public class AdvancedMedia3 extends AndroidNonvisibleComponent {

    private final ComponentContainer container;
    private final Context context;
    private final Form form;

    public AdvancedMedia3(ComponentContainer container) {
        super(container.$form());

        this.container = container;
        this.context = container.$context();
        this.form = container.$form();
    }
}
