package org.openmrs.module.registrationcore.api.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Location;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.module.idgen.IdentifierSource;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class IdentifierGeneratorTest {

    private static final String OPENMRS_IDENTIFIER_SOURCE_ID = "1";
    private static final String OPENMRS_GENERATED_IDENTIFIER = "10012NF";
    private static final String CUSTOM_MPI_IDENTIFIER_NAME = "ECID";
    private static final String CUSTOM_MPI_IDENTIFIER_ID = "2";
    private static final String CUSTOM_MPI_IDENTIFIER_VALUE = "16340061NF";

    @InjectMocks private IdentifierGenerator generator;
    @Mock private LocationService locationService;
    @Mock private IdentifierSourceService iss;
    @Mock private AdministrationService adminService;
    @Mock private PatientService patientService;

    @Mock private Location defaultLocation;
    @Mock private Location customLocation;
    @Mock private IdentifierSource identifierSource;
    @Mock private PatientIdentifierType identifierType;
    @Mock private PatientIdentifierType customMpiPatientIdentifierType;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetCorrectOpenMRSIdentifierSourceIdFromGP() throws Exception {
        mockOpenMRSIdentifierSourceGP();

        Integer actualId = generator.getOpenMrsIdentifierSourceId();
        verify(adminService).getGlobalProperty(RegistrationCoreConstants.GP_IDENTIFIER_SOURCE_ID);
        assertEquals(actualId, Integer.valueOf(OPENMRS_IDENTIFIER_SOURCE_ID));
    }

    @Test
    public void testGetIdentifierIdByName() throws Exception {
        mockCustomIdentifier();

        Integer actualId = generator.getIdentifierIdByName(CUSTOM_MPI_IDENTIFIER_NAME);

        verify(adminService).getGlobalProperty(
                RegistrationCoreConstants.GP_IDENTIFIER_SOURCE_ID_COMMON + CUSTOM_MPI_IDENTIFIER_NAME);
        assertEquals(actualId, Integer.valueOf(CUSTOM_MPI_IDENTIFIER_ID));
    }

    @Test(expected = APIException.class)
    public void testGetIdentifierSourceThrowExceptionOnInvalidPropertyValue() throws Exception {
        mockInvalidProperty();

        generator.getOpenMrsIdentifierSourceId();
    }

    @Test(expected = APIException.class)
    public void testGetIdentifierByNameThrowExceptionOnInvalidPropertyValue() throws Exception {
        mockInvalidProperty();

        generator.getIdentifierIdByName(CUSTOM_MPI_IDENTIFIER_NAME);
    }

    @Test
    public void testGenerateIdentifierWithNullLocation() throws Exception {
        mockDefaultLocation(defaultLocation);
        mockIdentifierSource(identifierSource);
        mockGeneratedIdentifier();

        PatientIdentifier patientIdentifier = generator.generateIdentifier(
                Integer.valueOf(OPENMRS_IDENTIFIER_SOURCE_ID), null);

        verify(locationService).getDefaultLocation();
        verify(iss).getIdentifierSource(Integer.valueOf(OPENMRS_IDENTIFIER_SOURCE_ID));
        verify(iss).generateIdentifier(identifierSource, null);
        assertEquals(patientIdentifier.getIdentifier(), OPENMRS_GENERATED_IDENTIFIER);
        assertEquals(patientIdentifier.getIdentifierType(), identifierType);
        assertEquals(patientIdentifier.getLocation(), defaultLocation);
    }

    @Test
    public void testGenerateIdentifierWithCustomLocation() throws Exception {
        mockDefaultLocation(defaultLocation);
        mockIdentifierSource(identifierSource);
        mockGeneratedIdentifier();

        PatientIdentifier patientIdentifier = generator.generateIdentifier(
                Integer.valueOf(OPENMRS_IDENTIFIER_SOURCE_ID), customLocation);

        verify(locationService, never()).getDefaultLocation();
        verify(iss).getIdentifierSource(Integer.valueOf(OPENMRS_IDENTIFIER_SOURCE_ID));
        verify(iss).generateIdentifier(identifierSource, null);
        assertEquals(patientIdentifier.getIdentifier(), OPENMRS_GENERATED_IDENTIFIER);
        assertEquals(patientIdentifier.getIdentifierType(), identifierType);
        assertEquals(patientIdentifier.getLocation(), customLocation);
    }

    @Test(expected = APIException.class)
    public void testGenerateIdentifierThrowExceptionOnEmptyDefaultLocation() throws Exception {
        mockDefaultLocation(null);

        generator.generateIdentifier(Integer.valueOf(OPENMRS_IDENTIFIER_SOURCE_ID), null);
    }

    @Test(expected = APIException.class)
    public void testGenerateIdentifierThrowExceptionOnEmptyIdentifierSource() throws Exception {
        mockDefaultLocation(defaultLocation);
        mockIdentifierSource(null);

        generator.generateIdentifier(Integer.valueOf(OPENMRS_IDENTIFIER_SOURCE_ID), null);
    }

    @Test
    public void testCreateIdentifierWithNullLocation() throws Exception {
        mockDefaultLocation(defaultLocation);
        mockPatientIdentifierType(customMpiPatientIdentifierType);

        PatientIdentifier identifier = generator.createIdentifier(
                Integer.valueOf(CUSTOM_MPI_IDENTIFIER_ID), CUSTOM_MPI_IDENTIFIER_VALUE, null);

        verify(locationService).getDefaultLocation();
        verify(patientService).getPatientIdentifierType(Integer.valueOf(CUSTOM_MPI_IDENTIFIER_ID));
        assertEquals(identifier.getIdentifier(), CUSTOM_MPI_IDENTIFIER_VALUE);
        assertEquals(identifier.getIdentifierType(), customMpiPatientIdentifierType);
        assertEquals(identifier.getLocation(), defaultLocation);
    }

    @Test
    public void testCreateIdentifierWithCustomLocation() throws Exception {
        mockDefaultLocation(defaultLocation);
        mockPatientIdentifierType(customMpiPatientIdentifierType);

        PatientIdentifier identifier = generator.createIdentifier(
                Integer.valueOf(CUSTOM_MPI_IDENTIFIER_ID), CUSTOM_MPI_IDENTIFIER_VALUE, customLocation);

        verify(locationService, never()).getDefaultLocation();
        verify(patientService).getPatientIdentifierType(Integer.valueOf(CUSTOM_MPI_IDENTIFIER_ID));
        assertEquals(identifier.getIdentifier(), CUSTOM_MPI_IDENTIFIER_VALUE);
        assertEquals(identifier.getIdentifierType(), customMpiPatientIdentifierType);
        assertEquals(identifier.getLocation(), customLocation);
    }

    @Test(expected = APIException.class)
    public void testCreateIdentifierThrowExceptionOnEmptyDefaultLocation() throws Exception {
        mockDefaultLocation(null);

        generator.createIdentifier(Integer.valueOf(CUSTOM_MPI_IDENTIFIER_ID), CUSTOM_MPI_IDENTIFIER_VALUE, null);
    }

    private void mockOpenMRSIdentifierSourceGP() {
        when(adminService.getGlobalProperty(RegistrationCoreConstants.GP_IDENTIFIER_SOURCE_ID))
                .thenReturn(OPENMRS_IDENTIFIER_SOURCE_ID);
    }

    private void mockCustomIdentifier() {
        when(adminService.getGlobalProperty(
                RegistrationCoreConstants.GP_IDENTIFIER_SOURCE_ID_COMMON + CUSTOM_MPI_IDENTIFIER_NAME))
                .thenReturn(CUSTOM_MPI_IDENTIFIER_ID);
    }

    private void mockInvalidProperty() {
        when(adminService.getGlobalProperty(anyString())).thenReturn("not_numeric_value");
    }

    private void mockDefaultLocation(Location location) {
        when(locationService.getDefaultLocation()).thenReturn(location);
    }

    private void mockIdentifierSource(IdentifierSource identifierSource) {
        when(iss.getIdentifierSource(Integer.valueOf(OPENMRS_IDENTIFIER_SOURCE_ID))).thenReturn(identifierSource);
        if (identifierSource != null)
            when(identifierSource.getIdentifierType()).thenReturn(identifierType);
    }

    private void mockGeneratedIdentifier() {
        when(iss.generateIdentifier(identifierSource, null)).thenReturn(OPENMRS_GENERATED_IDENTIFIER);
    }

    private void mockPatientIdentifierType(PatientIdentifierType customMpiPatientIdentifierType) {
        when(patientService.getPatientIdentifierType(Integer.valueOf(CUSTOM_MPI_IDENTIFIER_ID)))
                .thenReturn(customMpiPatientIdentifierType);
    }
}