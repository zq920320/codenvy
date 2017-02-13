'use strict';

describe('Delete team', () => {
  let loginPageObject, createTeamObject, deleteTeamObject;


  beforeAll(() => {
    loginPageObject = require('../../login/login.po');
    createTeamObject = require('../create-team/create-team.po');
    deleteTeamObject = require('./delete-team.po');

    // login
    browser.get('/');
    browser.waitForAngular();
    loginPageObject.findLoginFormElements();
    loginPageObject.fillInCredentials();
    loginPageObject.doLogin();
    browser.waitForAngular();

  });

  afterAll(() => {
    createTeamObject.navbarTeamsListItemElements.count().then((count) => {
      if (count) {
        // delete all teams
        deleteTeamObject.deleteAllTeams();
      }
    });
    browser.waitForAngular();
    // logout
    loginPageObject.findNavbarElements();
    loginPageObject.doLogout();
  });


  describe('delete team from team details', () => {
    let teamName = 'testTeam123456';

    it('should be zero team quantity', () => {
      // check for zero quantity
      expect(createTeamObject.navbarTeamsListItemElements.count()).toEqual(0);
    });

    it('should be create a new team', () => {
      // create a new team
      createTeamObject.createTeam(teamName);

      // check new quantity
      expect(createTeamObject.navbarTeamsListItemElements.count()).toEqual(1);
      browser.waitForAngular();
    });

    it('should be team detail URL', () => {
      // check team details URL
      let re = new RegExp('team\/' + teamName);
      expect(browser.getCurrentUrl()).toMatch(re);
    });

    it('check if delete team button is displayed', () => {
      // check delete team button
      expect(deleteTeamObject.deleteTeamButton.isDisplayed()).toBe(true);
    });

    it('should be delete', () => {
      expect(createTeamObject.navbarTeamsListItemElements.count()).toEqual(1);
      // press delete
      deleteTeamObject.deleteTeamButton.click().then(()=>{
        deleteTeamObject.deleteTeam(teamName);
      });
      expect(createTeamObject.navbarTeamsListItemElements.count()).toEqual(0);
    });
  });

});
