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
import {CodenvyTeam} from '../../../components/api/codenvy-team.factory';
import {CodenvyResourcesDistribution} from '../../../components/api/codenvy-resources-distribution.factory';
import {CodenvyResourceLimits} from '../../../components/api/codenvy-resource-limits';
import {CodenvyPermissions} from '../../../components/api/codenvy-permissions.factory';
import {CodenvyUser} from '../../../components/api/codenvy-user.factory';

/**
 * Controller for a managing team details.
 *
 * @author Ann Shumilova
 */
export class TeamDetailsController {
  /**
   * Team API interaction.
   */
  private codenvyTeam: CodenvyTeam;
  /**
   * Team resources API interaction.
   */
  private codenvyResourcesDistribution: CodenvyResourcesDistribution;
  /**
   * Permissions API interaction.
   */
  private codenvyPermissions: CodenvyPermissions;
  /**
   * User API interaction.
   */
  private codenvyUser: CodenvyUser;
  /**
   * Notifications service.
   */
  private cheNotification: any;
  /**
   * Location service.
   */
  private $location: ng.ILocationService;
  /**
   * Service for displaying dialogs.
   */
  private $mdDialog: angular.material.IDialogService;
  /**
   * Lodash library.
   */
  private lodash: any;
  /**
   * Current team's name. Comes from route path params.
   */
  private teamName: string;
  /**
   * Current team's data.
   */
  private team: any;
  /**
   * The list of allowed user actions.
   */
  private allowedUserActions: Array<string>;
  /**
   * New team's name (for renaming widget).
   */
  private newName: string;
  /**
   * If <code>true</code> - team with pointed name doesn't exist or cannot be accessed.
   */
  private invalidTeam: boolean;
  /**
   * Index of the selected tab.
   */
  private selectedTabIndex: number;
  /**
   * Team limits.
   */
  private limits: any;
  /**
   * Copy of limits before letting to modify, to be able to compare.
   */
  private limitsCopy: any;
  /**
   * Parent available resource limits, that can be redistributed to team.
   */
  private maxLimits: any;
  /**
   * Page loading state.
   */
  private isLoading: boolean;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor(codenvyTeam: CodenvyTeam, codenvyResourcesDistribution: CodenvyResourcesDistribution, codenvyPermissions: CodenvyPermissions,
              codenvyUser: CodenvyUser, $route: ng.route.IRouteService, $location: ng.ILocationService,
              $mdDialog: angular.material.IDialogService, cheNotification: any, lodash: any) {
    this.codenvyTeam = codenvyTeam;
    this.codenvyResourcesDistribution = codenvyResourcesDistribution;
    this.codenvyPermissions = codenvyPermissions;
    this.codenvyUser = codenvyUser;
    this.teamName = $route.current.params.teamName;
    this.$location = $location;
    this.$mdDialog = $mdDialog;
    this.cheNotification = cheNotification;
    this.lodash = lodash;

    this.allowedUserActions = [];

    let page = $route.current.params.page;
    if (!page) {
      $location.path('/team/' + this.teamName);
    } else {
      this.selectedTabIndex = 0;
      switch (page) {
        case 'settings':
          this.selectedTabIndex = 0;
          break;
        case 'developers':
          this.selectedTabIndex = 1;
          break;
        case 'workspaces':
          this.selectedTabIndex = 2;
          break;
        default:
          $location.path('/team/' + this.teamName);
          break;
      }
    }

    this.fetchTeamDetails();
  }

  /**
   * Fetches the team's details by it's name.
   */
  fetchTeamDetails(): void {
    this.team  = this.codenvyTeam.getTeamByName(this.teamName);
    this.newName = angular.copy(this.teamName);
    if (!this.team) {
      this.codenvyTeam.fetchTeamByName(this.teamName).then((team: any) => {
        this.team = team;
        this.fetchLimits();
        this.fetchUserPermissions();
      }, (error: any) => {
        this.invalidTeam = true;
      });
    } else {
      this.fetchLimits();
      this.fetchUserPermissions();
    }
  }

  /**
   * Fecthes permission of user in current team.
   */
  fetchUserPermissions(): void {
    this.codenvyPermissions.fetchTeamPermissions(this.team.id).then(() => {
      this.allowedUserActions = this.processUserPermissions();
    }, (error: any) => {
      this.isLoading = false;
      if (error.status === 304) {
        this.allowedUserActions = this.processUserPermissions();
      }
    });
  }

  /**
   * Process permissions to retrieve current user actions.
   *
   * @returns {Array} current user allowed actions
   */
  processUserPermissions(): Array<string> {
    let userId = this.codenvyUser.getUser().id;
    let permissions = this.codenvyPermissions.getTeamPermissions(this.team.id);
    let userPermissions = this.lodash.find(permissions, (permission: any) => {
      return permission.userId === userId;
    });
    return userPermissions ? userPermissions.actions : [];
  }

  /**
   * Checks whether user is allowed to perform pointed action.
   *
   * @param value action
   * @returns {boolean} <code>true</code> if allowed
   */
  isUserAllowedTo(value: string): boolean {
    return this.allowedUserActions ? this.allowedUserActions.indexOf(value) >= 0 : false;
  }

  /**
   * Returns whether current user can change team resource limits.
   *
   * @returns {boolean} <code>true</code> if can change resource limits
   */
  canChangeResourceLimits(): boolean {
    return (this.codenvyTeam.getPersonalAccount() && this.team) ? this.codenvyTeam.getPersonalAccount().id === this.team.parent : false;
  }

  /**
   * Fecthes defined team's limits (workspace, runtime, RAM caps, etc).
   */
  fetchLimits(): void {
    this.isLoading = true;
    this.codenvyResourcesDistribution.fetchTeamResources(this.team.id).then(() => {
      this.isLoading = false;
      this.processResources(this.codenvyResourcesDistribution.getTeamResources(this.team.id));
    }, (error: any) => {
      this.isLoading = false;
      if (error.status === 304) {
        this.processResources(this.codenvyResourcesDistribution.getTeamResources(this.team.id));
      }
    });

    this.codenvyResourcesDistribution.fetchTeamResources(this.team.parent).then(() => {
      this.processMaxValues(this.codenvyResourcesDistribution.getTeamResources(this.team.parent));
    }, (error: any) => {
      if (error.status === 304) {
        this.processMaxValues(this.codenvyResourcesDistribution.getTeamResources(this.team.parent));
      }
    });
  }

  /**
   * Process resources limits.
   *
   * @param resources resources to process
   */
  processResources(resources): void {
    let ramLimit = this.codenvyResourcesDistribution.getTeamResourceByType(this.team.id, CodenvyResourceLimits.RAM);
    let workspaceLimit = this.codenvyResourcesDistribution.getTeamResourceByType(this.team.id, CodenvyResourceLimits.WORKSPACE);
    let runtimeLimit = this.codenvyResourcesDistribution.getTeamResourceByType(this.team.id, CodenvyResourceLimits.RUNTIME);

    this.limits = {};
    this.limits.workspaceCap = workspaceLimit ? workspaceLimit.amount : undefined;
    this.limits.runtimeCap = runtimeLimit ? runtimeLimit.amount : undefined;
    this.limits.ramCap = ramLimit ? ramLimit.amount / 1000 : undefined;
    this.limitsCopy = angular.copy(this.limits);
  }

  /**
   * Process max available values (parent available resources).
   *
   * @param resources
   */
  processMaxValues(resources): void {
    let ramLimit = this.codenvyResourcesDistribution.getTeamResourceByType(this.team.parent, CodenvyResourceLimits.RAM);
    let workspaceLimit = this.codenvyResourcesDistribution.getTeamResourceByType(this.team.parent, CodenvyResourceLimits.WORKSPACE);
    let runtimeLimit = this.codenvyResourcesDistribution.getTeamResourceByType(this.team.parent, CodenvyResourceLimits.RUNTIME);

    this.maxLimits = {};
    this.maxLimits.workspaceCap = workspaceLimit ? workspaceLimit.amount : undefined;
    this.maxLimits.runtimeCap = runtimeLimit ? runtimeLimit.amount : undefined;
    this.maxLimits.ramCap = ramLimit ? ramLimit.amount / 1000 : undefined;
  }

  /**
   * Checks value whether provided value of provided type is valid.
   *
   * @param value value to be checked
   * @param type type of the resources
   * @returns {boolean} <code>true</code> if value is valid
   */
  isValidLimit(value: any, type: CodenvyResourceLimits): boolean {
    if (!this.maxLimits || !this.limitsCopy) {
      return true;
    }
    debugger;

    switch (type) {
      case CodenvyResourceLimits.RAM:
        return value <= (this.limitsCopy.ramCap || 0 + this.maxLimits.ramCap || 0);
      case CodenvyResourceLimits.WORKSPACE:
        return value <= (this.limitsCopy.workspaceCap || 0 + this.maxLimits.workspaceCap || 0);
      case CodenvyResourceLimits.RUNTIME:
        return value <= (this.limitsCopy.runtimeCap || 0 + this.maxLimits.runtimeCap || 0);
    }
  }

  /**
   * Confirms and performs team's deletion.
   *
   * @param event
   */
  deleteTeam(event: MouseEvent): void {
    let confirm = this.$mdDialog.confirm()
      .title('Would you like to delete team \'' + this.team.name + '\'?')
      .ariaLabel('Delete team')
      .ok('Delete it!')
      .cancel('Cancel')
      .clickOutsideToClose(true)
      .targetEvent(event);
    this.$mdDialog.show(confirm).then(() => {
      let promise = this.codenvyTeam.deleteTeam(this.team.id);
      promise.then(() => {
        this.$location.path('/workspaces');
        this.codenvyTeam.fetchTeams();
      }, (error: any) => {
        this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Team deletion failed.');
      });

      return promise;
    });
  }

  /**
   * Update team's details.
   *
   * @param invalid <true> if invalid data entered
   */
  updateTeamName(invalid: boolean): void {
    if (!invalid && this.newName && this.team && this.newName !== this.team.name) {
      this.team.name = this.newName;
      this.codenvyTeam.updateTeam(this.team).then(() => {
        this.codenvyTeam.fetchTeams().then(() => {
          this.$location.path('/team/' + this.newName);
        });
      }, (error: any) => {
        this.cheNotification.showError((error.data && error.data.message !== null) ? error.data.message : 'Rename team failed.');
      });
    }
  }

  /**
   * Update resource limits.
   *
   * @param invalid is form invalid
   * @param type type of the changed resource
   */
  updateLimits(invalid: boolean, type: CodenvyResourceLimits): void {
    if (invalid || !this.team || !this.limits || angular.equals(this.limits, this.limitsCopy)) {
      return;
    }

    let ramLimit = this.codenvyResourcesDistribution.getTeamResourceByType(this.team.id, CodenvyResourceLimits.RAM);
    let workspaceLimit = this.codenvyResourcesDistribution.getTeamResourceByType(this.team.id, CodenvyResourceLimits.WORKSPACE);
    let runtimeLimit = this.codenvyResourcesDistribution.getTeamResourceByType(this.team.id, CodenvyResourceLimits.RUNTIME);

    let resources = this.codenvyResourcesDistribution.getTeamResources(this.team.id);
    resources = angular.copy(resources);

    if (this.limits.ramCap) {
      resources = this.codenvyResourcesDistribution.setTeamResourceLimitByType(resources, CodenvyResourceLimits.RAM, (this.limits.ramCap * 1000));
    }

    if (this.limits.workspaceCap) {
      resources = this.codenvyResourcesDistribution.setTeamResourceLimitByType(resources, CodenvyResourceLimits.WORKSPACE, this.limits.workspaceCap);
    }

    if (this.limits.runtimeCap) {
      resources = this.codenvyResourcesDistribution.setTeamResourceLimitByType(resources, CodenvyResourceLimits.RUNTIME, this.limits.runtimeCap);
    }

    this.isLoading = true;
    this.codenvyResourcesDistribution.distributeResources(this.team.id, resources).then(() => {
      this.fetchLimits();
    }, (error: any) => {
      let resource = '';
      let value;
      switch (type) {
        case CodenvyResourceLimits.RAM:
          resource = 'workspace RAM cap';
          value = this.limits.ramCap;
          break;
        case CodenvyResourceLimits.WORKSPACE:
          resource = 'workspace cap';
          value = this.limits.workspaceCap;
          break;
        case CodenvyResourceLimits.RUNTIME:
          resource = 'running workspaces cap';
          value = this.limits.runtimeCap;
          break;
      }

      let errorMessage = 'Failed to set ' + resource + ' to ' + value + '.';
      this.cheNotification.showError((error.data && error.data.message !== null) ? errorMessage + '</br>Reason: ' + error.data.message : errorMessage);

      this.fetchLimits();
    });
  }

  /**
   * Returns the RAM resource type.
   *
   * @returns {CodenvyResourceLimits} the RAM resource type
   */
  getRAMResourceType(): CodenvyResourceLimits {
    return CodenvyResourceLimits.RAM;
  }

  /**
   * Returns the workspace resource type.
   *
   * @returns {CodenvyResourceLimits} the workspace resource type
   */
  getWorkspaceResourceType(): CodenvyResourceLimits {
    return CodenvyResourceLimits.WORKSPACE;
  }

  /**
   * Returns the workspace runtime resource type.
   *
   * @returns {CodenvyResourceLimits} the workspace runtime resource type
   */
  getRuntimeResourceType(): CodenvyResourceLimits {
    return CodenvyResourceLimits.RUNTIME;
  }
}
