package com.ibm.ada.api;

import com.ibm.ada.api.model.About;
import com.ibm.ada.api.model.auth.User;

/**
 * Some basic interface to fetch information about Ada.
 */
public interface Ada {

    About about();

    User authenticatedUser();

}
