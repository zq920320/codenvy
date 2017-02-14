'use strict';

describe('Create team > ', () => {
  let pageObject, loginPageObject, deleteTeamObject, helpers;

  beforeAll(() => {
    loginPageObject = require('../../login/login.po');
    deleteTeamObject = require('./../delete-team/delete-team.po');

    // login
    browser.get('/');
    browser.waitForAngular();
    loginPageObject.findLoginFormElements();
    loginPageObject.fillInCredentials();
    loginPageObject.doLogin();
    browser.waitForAngular();

    // delete all teams
    deleteTeamObject.deleteAllTeams();
  });

  afterAll(() => {
    //logout
    loginPageObject.findNavbarElements();
    loginPageObject.doLogout();
  });

  beforeEach(() => {
    pageObject = require('./create-team.po');
    helpers = require('../../helpers');
  });

  /*
   Title                Create team from left sidebar
   Description          A user can get the flow to create a team from left sidebar
   Sequence to execute  Click “Create Team” in left sidebar

   Expected result      Flow to “Create New Team” is displayed
   */
  describe('Create team from left sidebar >', () => {

    it('"Create team" link is available in sidebar', () => {
      expect(pageObject.navbarCreateTeamElement.isDisplayed()).toBeTruthy();
    });

    it('"Create team" link redirects to "Create new team" flow', () => {
      pageObject.navbarCreateTeamElement.click();
      expect($('ng-form[name="createTeamForm"]').isDisplayed()).toBeTruthy();
    });

  });

  /*
   Title                Team name special characters
   Description          A team name cannot have special characters
   Sequence to execute  Click “Create Team” in left sidebar
                        Give name “myteam&@”

   Expected result      It displays below the team name text field an error message:
                        “Team name must contain only letters and digits”
                        “Create Team” button is disabled
   */
  describe('Team name special characters >', () => {

    beforeEach(() => {
      browser.setLocation('/');
    });

    afterAll(() => {
      // delete all teams
      deleteTeamObject.deleteAllTeams();
    });

    it('"Create" button must be disabled', () => {
      pageObject.navbarCreateTeamElement.click();
      expect(pageObject.createTeamButtonElement.$('button').getAttribute('disabled')).toBeTruthy();
    });

    it('"Create" button must be disabled for wrong characters', () => {
      pageObject.navbarCreateTeamElement.click();
      pageObject.newTeamNameElement.sendKeys('myteam&@');

      expect(pageObject.createTeamButtonElement.$('button').getAttribute('disabled')).toBeTruthy();

      expect(pageObject.createTeamErrorMessage.isDisplayed()).toBeTruthy();
    });

    it('"Create" button must be enabled for valid characters', () => {
      pageObject.navbarCreateTeamElement.click();
      pageObject.newTeamNameElement.sendKeys(helpers.getRandomName('myteam'));

      expect(pageObject.createTeamButtonElement.$('button').getAttribute('disabled')).toBeFalsy();
    });

  });

  /*
   Title                  Team name with digits
   Description            A team name can have digit characters
   Sequence to execute    Click “Create Team” in left sidebar
                          Give name “myteam23”
                          Click to “Create Team”

   Expected result        Team is created properly
   */
  describe('Team name with digits >', () => {

    beforeAll(() => {
      browser.setLocation('/');
    });

    afterAll(() => {
      // delete all teams
      deleteTeamObject.deleteAllTeams();
    });

    it('"Create" button must be disabled', () => {
      pageObject.navbarCreateTeamElement.click();
      pageObject.newTeamNameElement.sendKeys('myteam23');
      pageObject.createTeamButtonElement.click();

      expect( pageObject.navbarTeamsListElement.element(by.cssContainingText('.navbar-item', 'myteam23')).isDisplayed() ).toBeTruthy();
    });

  });

  /*
   Title                Infinite number of teams
   Description          A user has the ability to create as many teams he wants
   Sequence to execute  Click “Create Team” in left sidebar
                        Give name “Team_00”
                        Click to “Create Team”
                        Repeat 10 times (and change Give name “Team_00” to “Team_01”...)

   Expected result      All teams are created and visible in the left sidebar
   */
  describe('Infinite number of teams >', () => {

    beforeAll(() => {
      browser.setLocation('/');
    });

    afterAll(() => {
      // delete all teams
      deleteTeamObject.deleteAllTeams();
    });

    for (let i = 0; i < 10; i++) {
      it('Create 10 teams', () => {
        let teamName = helpers.getRandomName('Team');
        pageObject.createTeam(teamName);

        expect( pageObject.navbarTeamsListElement.element(by.cssContainingText('.navbar-item', teamName)).isDisplayed() ).toBeTruthy();
        expect(pageObject.navbarTeamsListItemElements.count()).toEqual(i + 1);
      });
    }

  });

});
