/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.register;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.htmlformentry.HtmlForm;
import org.openmrs.module.register.db.hibernate.Register;
import org.openmrs.module.register.db.hibernate.RegisterType;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;

public class RegisterServiceTest extends BaseModuleContextSensitiveTest {

	protected static final String INITIAL_DATA_XML = "resources/org/openmrs/module/register/RegisterServiceTest-initialData.xml";
	private RegisterService registerService;
	private EncounterService encounterService;
	private PersonService personService;
	private PatientService patientService;
	private LocationService locationService;
	private UserService userService;

	/**
	 * Run this before each unit test in this class. This adds a bit more data
	 * to the base data that is done in the "@Before" method in
	 * {@link BaseContextSensitiveTest} (which is run right before this method).
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		executeDataSet(INITIAL_DATA_XML);
		
		registerService = Context.getService(RegisterService.class);
		encounterService = Context.getService(EncounterService.class);
		personService = Context.getService(PersonService.class);
		patientService = Context.getService(PatientService.class);
		locationService = Context.getService(LocationService.class);
		userService = Context.getUserService();
	}

    @Test
    public void shouldReturnAllRegisters() {
        List<Register> registers = registerService.getRegisters(true);
        assertEquals(2, registers.size());
    }

	@Test
	public void shouldDeleteAnEncounterFromARegister() throws Exception {
		Patient patient = createPatient();

		Location location = new Location();
		location.setName("Test Factory");
		locationService.saveLocation(location);
		
		Person provider = new Person();
		provider.setGender("male");
		personService.savePerson(provider);

		Register register = createRegister();
		Form form = register.getHtmlForm().getForm();

		Encounter encounter = new Encounter();
		encounter.setPatient(patient);
		encounter.setProvider(provider);
		encounter.setLocation(location);
		encounter.setEncounterDatetime(new Date());

		encounter.setForm(form);
		encounterService.saveEncounter(encounter);
		
		assertEquals(1, registerService.getEncounterCount(form, location).intValue());
		
		registerService.deleteEncounter(encounter.getId());
		assertNotNull(encounterService.getEncounter(encounter.getId()));
		assertEquals(0, registerService.getEncounterCount(form, location).intValue());
		assertEquals(0, registerService.getEncounters(form, location, 1, 0).size());
	}

	private Register createRegister() {
		User creator = new User();
		creator.setPerson(new Person());
		creator.addName(new PersonName("Benjamin", "A", "Wolfe"));
		creator.setUsername("bwolfe");
		creator.getPerson().setGender("M");
		userService.saveUser(creator, "MyAwes0m3P455w0rd");
		
		RegisterType registerType = new RegisterType();
		registerType.setName("test register type");
		registerType.setUuid("abcdefg");
		registerType.setCreator(creator);
		registerType.setDateCreated(new Date());

		Register register = new Register();
		register.setName("register");
		register.setRegisterType(registerType);
		
		HtmlForm htmlForm = new HtmlForm();
		htmlForm.setUuid("abcdefg");
		htmlForm.setXmlData("<form />");
		htmlForm.setCreator(creator);
		htmlForm.setDateCreated(new Date());
		
		Form form = new Form();
		form.setName("test form");
		form.setVersion("test1.1");
		form.setBuild(1);
		form.setDateCreated(new Date());
		form.setCreator(creator);
		htmlForm.setForm(form);
		register.setHtmlForm(htmlForm);
		
		registerService.saveRegister(register);
		Context.flushSession();
		return register;
	}

	private Patient createPatient() throws Exception {
		Patient patient = new Patient();
		
		PersonName pName = new PersonName();
		pName.setGivenName("Tom");
		pName.setMiddleName("E.");
		pName.setFamilyName("Patient");
		patient.addName(pName);
		
		PersonAddress pAddress = new PersonAddress();
		pAddress.setAddress1("123 My street");
		pAddress.setAddress2("Apt 402");
		pAddress.setCityVillage("Anywhere city");
		pAddress.setCountry("Some Country");
		Set<PersonAddress> pAddressList = patient.getAddresses();
		pAddressList.add(pAddress);
		patient.setAddresses(pAddressList);
		patient.addAddress(pAddress);
		//patient.removeAddress(pAddress);
		
		patient.setDeathDate(new Date());
		//patient.setCauseOfDeath("air");
		patient.setBirthdate(new Date());
		patient.setBirthdateEstimated(true);
		patient.setGender("male");
		
		List<PatientIdentifierType> patientIdTypes = patientService.getAllPatientIdentifierTypes();
		assertNotNull(patientIdTypes);
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		patientIdentifier.setIdentifier("123-0");
		patientIdentifier.setIdentifierType(patientIdTypes.get(0));
		patientIdentifier.setLocation(new Location(1));
		
		Set<PatientIdentifier> patientIdentifiers = new LinkedHashSet<PatientIdentifier>();
		patientIdentifiers.add(patientIdentifier);
		
		patient.setIdentifiers(patientIdentifiers);
		
		patientService.savePatient(patient);
		Patient createdPatient = patientService.getPatient(patient.getPatientId());
		assertNotNull(createdPatient);		
		assertNotNull(createdPatient.getPatientId());
		
		return createdPatient;
	}
    @Test
    public void shouldCreateARegister() throws Exception {
    	Register register = createRegister();
        Assert.assertNotNull(register.getId());
    }

    @Test
    public void shouldNotReturnRetiredRegisters() {
        List<Register> registers = registerService.getRegisters(false);
        assertEquals(1, registers.size());
        for (Register register : registers) {
            assertFalse(register.getRetired());
        }
    }

    @Test
    public void shouldReturnRegistersGivenARegisterId() {
        Register register = registerService.getRegister(112);
        assertEquals("Sample Visit register2", register.getName());
    }

    @Test
    public void shouldDeleteAGivenRegisterId() {
    	registerService.deleteRegister(111);
        List<Register> registerList = registerService.getRegisters(true);
        for (Register register : registerList) {
            if (111 == register.getId()) {
                fail();
            }
        }
    }

    @Test
    public void shouldReturnRegisterTypes() {
        List<RegisterType> registerTypes = registerService.getRegisterTypes();
        assertEquals(2,registerTypes.size());
    }

    @Test
    public void shouldReturnRegisterTypeGivenItsId() {
        RegisterType registerType = registerService.getRegisterType(101);
        assertEquals(101,registerType.getId().intValue());
    }

    @Test
    public void shouldReturnTheCorrespondingEncounters() {
        List<Encounter> encounters = registerService.getEncountersForRegisterByLocation(111, 1,2,1);
        assertEquals(2,encounters.size());
    }

	@Test
	@Verifies(value = "should get all the encounter with the given form and location id for the given page and pagesize", method = "getEncounters(Form, Location)")
	public void getEncounters_shouldGetAllTheEncountersWithTheGivenFormAndLocationId()
			throws Exception {
		
		FormService formService = Context.getFormService();
		LocationService locService = Context.getLocationService();
		
		List<Encounter> encountersPage1 = registerService.getEncounters(formService.getForm(1), locService.getLocation(1), 2, 1);
		assertEquals(2, encountersPage1.size());
		
		List<Encounter> encountersPage2 = registerService.getEncounters(formService.getForm(1), locService.getLocation(1), 2, 2);
		assertEquals(2, encountersPage2.size());

		List<Encounter> encountersPage3 = registerService.getEncounters(formService.getForm(1), locService.getLocation(1), 2, 3);
		assertEquals(1, encountersPage3.size());

	}
	
	@Test
	@Verifies(value = "should get all the encounter with the given form and location id for the given page and pagesize with encounterDatetime & date created in Ascending order", method = "getEncounters(Form, Location)")
	public void getEncounters_shouldGetAllTheEncountersWithTheGivenFormAndLocationIdForGivenPageAndPageSizeWithEncounterDateAndDateCreatedInAscOrder()
			throws Exception {		
		FormService formService = Context.getFormService();
		LocationService locService = Context.getLocationService();
		
		List<Encounter> encountersPage1 = registerService.getEncounters(formService.getForm(1), locService.getLocation(1), 5, 1);
		assertEquals(5, encountersPage1.size());
			
		for(int i=0;i<encountersPage1.size()-1;i++){
			assertTrue(encountersPage1.get(i).getEncounterDatetime().before(encountersPage1.get(i+1).getEncounterDatetime()));
		}

	}
	
	@Test
	@Verifies(value = "should get the total  encounter count with the given form and location id ", method = "getEncounterCount(Form, Location)")
	public void getEncounterCount_shouldGetTotalEncounterCountWithTheGivenFormAndLocationId()
			throws Exception {
		FormService formService = Context.getFormService();
		LocationService locService = Context.getLocationService();
		Integer encounterCount = registerService.getEncounterCount(formService.getForm(1), locService.getLocation(1));
		assertEquals(5, encounterCount.intValue());
	}
}
