'use strict';

describe('Create team > ', () => {
  let createTeamPageObject, loginPageObject, deleteTeamObject, helpers;

  beforeAll(() => {
    browser.get('/');

    loginPageObject = require('../../login/login.po');
    deleteTeamObject = require('./../delete-team/delete-team.po');
  });

  afterAll(() => {
    //logout
  });

  beforeEach(() => {
    createTeamPageObject = require('./create-team.po');
    helpers = require('../../helpers');

    browser.setLocation('/');
  });

  /*
   Description            When creating a team, it is possible to invite other developers
   Sequence to execute    Use two account:
                           - Account1
                           - Account2

                           - Login with Account1
                           - Click “Create Team” in left sidebar
                           - Give it name “team-existing-users”
                           - In section “Developers”, click button “Add”
                           - A small popup is displayed
                           - Enter email from “Account2”
                           - Click “Add” in the popup
                           - A small green check icon is display before the email
                           - The developer is added into the table in “Developers” section with a green check icon
                           - Click “Create Team” in the “Create New Team” form

   Expected result          - From Account1, go into the team details and “Developers” tab, you can see that the developer is a member of the team
                            - From Account2, you can see that you have been added into the team
                            - Account2’s email received a notification about joining the team
   */
  describe('When creating a team, it is possible to invite other developers >', () => {

    it('Create team in Account #1', () => {
      let devs = helpers.getDevs();

      let dev1email = helpers.getEmail(devs[0]),
          dev1password = helpers.getPassword(devs[0]);

      loginPageObject.login(dev1email, dev1password);

      // go to "Create new team" page
      createTeamPageObject.gotoCreatePage();

      // set team name
      createTeamPageObject.newTeamNameElement.sendKeys('team-existing-users');

      // click on "Add" button
      createTeamPageObject.addDevButtonElement.click();

      // fill in an email
      let dev2email = helpers.getEmail(devs[1]);
      createTeamPageObject.memberEmailsInputElement.sendKeys(dev2email);
      // invite a developer
      createTeamPageObject.memberAddButtonElement.click();

      let invitedDevListItemElement = createTeamPageObject.getInvitedDevListItemElement(dev2email);

      // invited developer is in the list
      expect(invitedDevListItemElement).toBeTruthy();
      expect(invitedDevListItemElement.$('.user-exists-checked').isDisplayed()).toBeTruthy();

      // create team
      this.createTeamButtonElement.click();

      loginPageObject.logout();
    });



  });



});
