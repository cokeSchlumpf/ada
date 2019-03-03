package com.ibm.ada.api;

import com.ibm.ada.model.AboutTO;
import com.ibm.ada.model.auth.User;

/**
 * Some basic interface to fetch information about Ada.
 */
public interface Ada {

    AboutTO about();

    User authenticatedUser();

}
