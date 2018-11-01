
### Test with [CF Dev](https://github.com/cloudfoundry-incubator/cfdev)

```
uaac target --skip-ssl-validation uaa.dev.cfdev.sh
uaac token client get admin -s admin-client-secret
uaac client add demo_nozzle \
  --name demo_nozzle \
  --secret demo_secret \
  --authorized_grant_types client_credentials,refresh_token \
  --authorities doppler.firehose,cloud_controller.global_auditor
```