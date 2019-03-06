package com.ibm.ada.api;

import com.ibm.ada.model.About;
import com.ibm.ada.model.auth.User;

/**
 * Some basic interface to fetch information about Ada.
 */
public interface Ada {

    About about();

    User authenticatedUser();

}
