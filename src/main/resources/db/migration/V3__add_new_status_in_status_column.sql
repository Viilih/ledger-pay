alter table accounts drop constraint accounts_status_check;

alter table accounts add constraint accounts_status_check check ( status in ('ACTIVE', 'BLOCKED', 'CLOSED',
                                                                             'DEACTIVATED') )