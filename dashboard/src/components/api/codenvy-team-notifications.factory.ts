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

enum TEAM_EVENTS {ORGANIZATION_MEMBER_ADDED, ORGANIZATION_MEMBER_REMOVED, ORGANIZATION_REMOVED, ORGANIZATION_RENAMED}


/**
 * This class is handling the notifications per each team.
 *
 * @author Ann Shumilova
 */
export class CodenvyTeamNotifications {

  $log: ng.ILogService;
  cheWebsocket: any;
  applicationNotifications: any;
  TEAM_CHANNEL: string = 'organization:';

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(cheWebsocket: any, applicationNotifications: any, $log) {
    this.cheWebsocket = cheWebsocket;
    this.applicationNotifications = applicationNotifications;
    this.$log = $log;
  }


  subscribeTeamNotifications(teamId: string) {
    let bus = this.cheWebsocket.getBus();
    bus.subscribe(this.TEAM_CHANNEL + teamId, (message: any) => {
      console.log(message);
      this.applicationNotifications.addInfoNotification("Team Renamed", "Team Renamed");
    });
  }

}
