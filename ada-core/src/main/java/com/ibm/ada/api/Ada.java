package com.ibm.ada.api;

import com.ibm.ada.model.AboutTO;
import com.ibm.ada.model.UserTO;

/**
 * Some basic interface to fetch information about Ada.
 */
public interface Ada {

    AboutTO about();

    UserTO authenticatedUser();

}
