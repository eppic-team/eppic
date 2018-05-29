/**
 * A package containing DTO classes, needed for the GWT application to be able
 * to transfer (serialize/deserialize) hibernate data.
 * 
 * See http://www.gwtproject.org/articles/using_gwt_with_hibernate.html
 * 
 * Quoting that documentation:
 * 
 * The DTO is a simple POJO only containing simple data fields that we can access on 
 * the client-side to display on the application page. The Hibernate objects can then 
 * be constructed from the data in our data transfer objects. The DTOs themselves will 
 * only contain the data we want to persist, but none of the lazy loading or persistence 
 * logic added by the Hibernate Javassist to their Hibernate counterparts.
 */
package eppic.model.dto;