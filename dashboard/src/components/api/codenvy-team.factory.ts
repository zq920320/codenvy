/*
 *  [2015] - [2017] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
'use strict';

import {CodenvyTeamRoles} from './codenvy-team-roles';
import {CodenvyTeamNotifications} from './codenvy-team-notifications.factory';
import {CodenvyUser} from './codenvy-user.factory';

interface ITeamsResource<T> extends ng.resource.IResourceClass<T> {
  getTeams(): ng.resource.IResource<T>,
  createTeam(data: {name: string, parent: string}): ng.resource.IResource<T>,
  fetchTeam(data: {id: string}): ng.resource.IResource<T>,
  deleteTeam(data: {id: string}): ng.resource.IResource<T>,
  updateTeam(data: {id: string}, team: any): ng.resource.IResource<T>,
  findTeam(data: {teamName: string}): ng.resource.IResource<T>
}

/**
 * This class is handling the interactions with Team management API.
 *
 * @author Ann Shumilova
 */
export class CodenvyTeam {
  /**
   * Angular Resource service.
   */
  private $resource: ng.resource.IResourceService;
  private $q: ng.IQService;
  private lodash: any;
  /**
   * Teams map by team's id.
   */
  private teamsMap : Map<string, any> = new Map();
  /**
   * Array of teams.
   */
  private teams : any = [];
  /**
   * The registry for managing available namespaces.
   */
  private cheNamespaceRegistry : any;
  /**
   * The Codenvy user API.
   */
  private codenvyUser : CodenvyUser;
  /**
   * The Codenvy Team notifications.
   */
  codenvyTeamNotifications: CodenvyTeamNotifications;
  /**
   * User's personal account.
   */
  private personalAccount: any;
  /**
   * Client for requesting Team API.
   */
  private remoteTeamAPI: ITeamsResource<any>;
  /**
   * Deferred object which will be resolved when teams are fetched
   */
  private fetchTeamsDefer: ng.IDeferred<any>;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($resource: ng.resource.IResourceService, $q: ng.IQService, lodash: any, cheNamespaceRegistry: any, codenvyUser: CodenvyUser,
              codenvyTeamNotifications: CodenvyTeamNotifications) {
    this.$resource = $resource;
    this.$q = $q;
    this.lodash = lodash;
    this.cheNamespaceRegistry = cheNamespaceRegistry;
    this.codenvyUser = codenvyUser;
    this.codenvyTeamNotifications = codenvyTeamNotifications;

    this.remoteTeamAPI = <ITeamsResource<any>>$resource('/api/organization', {}, {
      getTeams: {method: 'GET', url: '/api/organization', isArray: true},
      fetchTeam: {method: 'GET', url: '/api/organization/:id'},
      createTeam: {method: 'POST', url: '/api/organization'},
      deleteTeam: {method: 'DELETE', url: '/api/organization/:id'},
      updateTeam: {method: 'POST', url: '/api/organization/:id'},
      findTeam: {method: 'GET', url: '/api/organization/find?name=:teamName'}
    });

    this.fetchTeamsDefer = this.$q.defer();
    const fetchTeamsPromise = this.fetchTeamsDefer.promise;
    this.cheNamespaceRegistry.setFetchPromise(fetchTeamsPromise);
  }

  /**
   * Request the list of available teams.
   *
   * @returns {PromiseLike<TResult>|Promise<TResult>}
   */
  fetchTeams(): ng.IPromise<any> {
    let defer = this.$q.defer();

    let promise = this.remoteTeamAPI.getTeams().$promise;

    // process the result into map and array:
    promise.then((teams: any) => {
      this.codenvyUser.fetchUser().then(() => {
        this.processTeams(teams, this.codenvyUser.getUser());
        defer.resolve();
      }, (error) => {
        if (error.status === 304) {
          this.processTeams(teams, this.codenvyUser.getUser());
          defer.resolve();
        } else {
          defer.reject();
        }
      });
    }, (error: any) => {
      defer.reject(error);
    });

    return defer.promise.then(() => {
      this.fetchTeamsDefer.resolve();
    });
  }

  /**
   * Process teams to retrieve personal account (name of the organization === current user's name) and
   * teams (organization with parent).
   *
   * @param teams
   * @param user
   */
  processTeams(teams: Array<any>, user: any): void {
    this.teamsMap = new Map();
    this.teams = [];
    this.cheNamespaceRegistry.getNamespaces().length = 0;

    let name = user.name;
    // detection personal account (organization which name equals to current user's name):
    this.personalAccount = this.lodash.find(teams, (team: any) => {
      return team.qualifiedName === name;
    });

    if (this.personalAccount) {
      // display personal account as "personal" on UI, namespace(id) stays the same for API interactions:
      this.cheNamespaceRegistry.getNamespaces().push({id: this.personalAccount.qualifiedName, label: 'personal', location: '/billing'});
    }

    teams.forEach((team : any) => {
      this.teamsMap.set(team.id, team);
      // team has to have parent (root organizations are skipped):
      if (team.parent) {
        this.teams.push(team);
        this.codenvyTeamNotifications.subscribeTeamNotifications(team.id);
      }
    });

    this.teams.forEach((team : any) => {
      // team has to have parent (root organizations are skipped):
      if (team.parent) {
        this.cheNamespaceRegistry.getNamespaces().push({id: team.qualifiedName, label: this.getTeamDisplayName(team), location: '/team/' + team.qualifiedName});
      }
    });
  }

  /**
   * Return current user's personal account.
   *
   * @returns {any} personal account
   */
  getPersonalAccount(): any {
    return this.personalAccount;
  }

  /**
   * Returns the array of teams.
   *
   * @returns {Array<any>} the array of teams
   */
  getTeams(): Array<any> {
    return this.teams;
  }

  /**
   * Requests team by it's id.
   *
   * @param name the team's name
   * @returns {ng.IPromise<any>} result promise
   */
  fetchTeamById(id: string): ng.IPromise<any> {
    let promise = this.remoteTeamAPI.fetchTeam({'id' : id}).$promise;
    let resultPromise = promise.then((data) => {
      this.teamsMap.set(id, data);
    });
    return resultPromise;
  }

  /**
   * Requests team by it's name.
   *
   * @param name the team's name
   * @returns {ng.IPromise<any>} result promise
   */
  fetchTeamByName(name: string): ng.IPromise<any> {
    let promise = this.remoteTeamAPI.findTeam({'teamName' : name}).$promise;
    return promise;
  }

  /**
   * Returns team by it's name.
   *
   * @param name team's name
   * @returns {any} team or <code>null</code> if not found
   */
  getTeamByName(name: string): any {
    for (let i = 0; i < this.teams.length; i++) {
      if (this.teams[i].qualifiedName === name) {
        return this.teams[i];
      }
    };

    return null;
  }

  /**
   * Returns team by it's id.
   *
   * @param team's id
   * @returns {any} team or <code>null</code> if not found
   */
  getTeamById(id: string): any {
    return this.teamsMap.get(id);
  }

  /**
   * Creates new team with pointed name.
   *
   * @param name the name of the team to be created
   * @returns {ng.IPromise<any>} result promise
   */
  createTeam(name: string): ng.IPromise<any> {
    let data = {name : name, parent: this.personalAccount.id};
    let promise = this.remoteTeamAPI.createTeam(data).$promise;
    return promise;
  }

  /**
   * Delete team by pointed id.
   *
   * @param id team's id to be deleted
   * @returns {ng.IPromise<any>} result promise
   */
  deleteTeam(id: string): ng.IPromise<any> {
    let promise = this.remoteTeamAPI.deleteTeam({'id' : id}).$promise;
    return promise;
  }

  /**
   * Update team's info.
   *
   * @param team the team info to be updated
   * @returns {ng.IPromise<any>} result promise
   */
  updateTeam(team: any): ng.IPromise<any> {
    let promise = this.remoteTeamAPI.updateTeam({'id' : team.id}, team).$promise;
    return promise;
  }

  /**
   * Forms the list of roles based on the list of actions
   *
   * @param actions array of actions
   * @returns {Array<any>} array of roles
   */
  getRolesFromActions(actions: Array<string>): Array<any> {
    let roles = [];
    let teamRoles = CodenvyTeamRoles.getValues();
    teamRoles.forEach((role: any) => {
      if (this.lodash.difference(role.actions, actions).length === 0) {
        roles.push(role);
      }
    });

    // Avoid roles intake (filter if any role's action is subset of any other):
    roles = this.lodash.filter(roles, (role: any) => {
      return !this._checkIsSubset(role, roles);
    });

    return roles;
  }

  /**
   * Checks the actions in provided role to be part (subset) of any other role's actions.
   *
   * @param role role to be checked
   * @param roles list of roles
   * @returns {boolean} <code>true</code> if subset
   * @private
   */
  _checkIsSubset(role: any, roles: Array<any>): boolean {
    let isSubset = false;
    for (let i = 0; i < roles.length; i++) {
      let r = roles[i];
      // Checks provided role's action is subset of any other role's actions in the roles list:
      if (role.actions.length === this.lodash.intersection(role.actions, r.actions).length && role.actions.length !== r.actions.length) {
        return true;
      }
    }

    return isSubset;
  }

  /**
   * Forms the list actions based on the list of roles.
   *
   * @param roles array of roles
   * @returns {Array<string>} actions array
   */
  getActionsFromRoles(roles: Array<any>): Array<string> {
    let actions = [];
    roles.forEach((role: any) => {
      actions = actions.concat(role.actions);
    });

    return actions;
  }

  getTeamDisplayName(team: any): string {
    let teamNames = this.lodash.pluck(this.teams, 'name');
    let size = this.lodash.pull(teamNames, team.name).length;

    return (this.teams.length - size) > 1 ? team.qualifiedName : team.name;
  }
}
