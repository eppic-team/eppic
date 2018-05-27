package eppic.dtomodel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * All contacts from all interfaces
 * 
 * @author duarte_j
 *
 */
public class ContactsList  extends HashMap<Integer, List<Contact>> implements Serializable {

	private static final long serialVersionUID = 1L;

}
