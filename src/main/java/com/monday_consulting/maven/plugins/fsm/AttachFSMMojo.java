package com.monday_consulting.maven.plugins.fsm;

/*
Copyright 2016-2020 Monday Consulting GmbH

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * This Mojo attaches the assembled FSM as a project artifact.
 *
 * @author Oliver Degener
 * @since 1.6.0
 */
@SuppressWarnings("unused")
@Mojo(name = "attachFSM", defaultPhase = LifecyclePhase.PACKAGE)
class AttachFSMMojo extends BaseFSMMojo {

    @Override
    public void execute() {
        attachFSM();
    }

}
