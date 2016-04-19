package com.connexta.arbitro.attr;

import com.connexta.arbitro.cond.Evaluatable;

import java.net.URI;

/**
 *
 */
public abstract class AbstractDesignator implements Evaluatable {

    public abstract URI getId();

}
