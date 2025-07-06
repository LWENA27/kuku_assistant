#!/bin/bash
sed -i "s/callback\.onSuccess();/callback.onSuccess(null);/g" app/src/main/java/com/example/fowltyphoidmonitor/services/auth/AuthManager.java
