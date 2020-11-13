/*pipeline script to check first the install,
then run the Cloud Smoke Tests
Author :XKK / MSI26
*/

def abaSyms = '''
call aba_syms
'''
def tck = '''
call tck_init
'''
def tckProfile = '''
call tck_profile simqatools
'''
def sikuliRun =""" 
call sikulirun -level R423 -use_install_dir D:/wdir/AutoInstall/${params.PODID}_${params.ROLE} -no_minimize -perf -save -package D:/AutomaticTests/2021x_SCM -verbose -cloud -test ST_OnCloud_${params.ROLE} -pod_info D:/temp/plm_pod_data.xml > d:/temp/out 2>&1
"""
pipeline {
    agent { label 'punpmwinsim02' }
    parameters {
        string(name: 'BASEURL', defaultValue:'https://eu1-cstf200511-ifwe.3dexperience.3ds.com/', description: 'Enter Pod URL')
        string(name: 'USER_NAME', defaultValue: 'aheua_euw1', description: 'Username')
        string(name: 'PASSWORD', defaultValue: 'Test1234', description: 'Password')
        string(name: 'EMAIL', defaultValue: 'aheua_euw1@dispostable.com', description: 'Enter the user email')
        string(name: 'USERDISPLAYNAME', defaultValue: 'AHEUA EUW1', description: 'Display Name for User')
        string(name: 'PODID', defaultValue: '23cstf200511', description: 'Pod Id, required for folder creation and version check')
        string(name: 'TENANTID', defaultValue: 'DSQAL005', description: 'Tenant Id')
        string(name: 'GEO', defaultValue: 'euw1', description: 'Tenant GEO')
        string(name: 'ROLE', defaultValue: 'AHEUA', description: 'Role Trigram')
        string(name: 'APPNAME', defaultValue: 'Fluid Scenario Creation', description: 'App Name')
        string(name: 'BROWSER', defaultValue: 'chrome', description: 'Browser')
        string(name: 'LICENSE', defaultValue: 'Credits', description: 'Credits/Tokens')
        string(name: 'MODEL', defaultValue: 'AHEUA_Solid_Radiation.3dxml', description: '3DXML model with extension')
    }//parameters


    stages {
        stage('Check Install exists and UnInstall') {
            steps {
                script {
                    def previous_install_exists = fileExists 'D:/wdir/versions_to_delete'
                    if (previous_install_exists) {
                        bat 'rm -f D:/wdir/versions_to_delete'
                    }
                }
                //check for the previous builds exists
                bat tck + tckProfile + "call cloud_build_checker -p ${params.PODID} -r ${params.ROLE}"
                script {
                    def install_exists = fileExists 'D:/wdir/install_required'
                    def previous_install_exists = fileExists 'D:/wdir/versions_to_delete'
                    if (install_exists) {
                        println('Install required')
                        env.INSTALL = 'True'
                        bat 'rm -f D:/wdir/install_required'
                        if (previous_install_exists) {
                            //uninstall of previous build happens through this script
                            bat 'D:\\wdir\\do_not_delete\\3dx_uninstall.lnk'
                            bat 'rm -f D:/wdir/versions_to_delete'
                            bat 'if exist D:\\wdir\\AutoInstall (rmdir D:\\wdir\\AutoInstall /S /Q)'
                        }
                    }
                    else {
                        println('Install not required')
                        env.INSTALL = 'False'
                    }
                }//script
            }//steps
        }//stage
        stage('Install Required') {
            when { environment name: 'INSTALL', value:'True' }
            steps {
                bat tck + tckProfile + "call config_file_generator -w \"${params.BASEURL}\" -u ${params.USER_NAME} -p ${params.PASSWORD} -e ${params.EMAIL} -n \"${params.USERDISPLAYNAME}\" -po ${params.PODID} -t ${params.TENANTID} -r ${params.ROLE} -a \"${params.APPNAME}\" -b ${params.BROWSER} -g ${params.GEO} -m ${params.MODEL}"
                bat "D:\\AutomaticTests\\2021x_SCM\\Launchers\\RunCloudInstallTest.bat > d:/temp/install_out 2>&1"
                script {
                    post_install_exists = fileExists "D:/wdir/AutoInstall/${PODID}_${ROLE}/win_b64/code/bin/3DEXPERIENCE.exe"
                    if (post_install_exists) {
                        script{
                            def readContent = readFile "d:/wdir/AutoInstall/${PODID}_${ROLE}/CATEnv/Env.txt"
                            writeFile file: "d:/wdir/AutoInstall/${PODID}_${ROLE}/CATEnv/Env.txt", text: readContent +"\r\nCATForceNotCertifiedGraphics=TRUE"
                            writeFile file: "d:/wdir/AutoInstall/${PODID}_${ROLE}/CATEnv/Env.txt", text: readContent +"\r\nCNEXTOUTPUT=d:\\wdir\\traces\\cnext_client.out"
                        }
                        println 'Installation Successull.. Pipeline continues..'
                    }
                    else {
                        bat "type d:\\temp\\install_out"
                        error '!!!! Install is NOT successful !!!!'
                    }//else
                }//script
            }//steps
        }//stage
        stage('Config File Setting Stage') {
            steps {
                bat tck + tckProfile + "call config_file_generator -f cloud_config_start_apps.json -w \"${params.BASEURL}\" -u ${params.USER_NAME} -p ${params.PASSWORD} -e ${params.EMAIL} -n \"${params.USERDISPLAYNAME}\" -po ${params.PODID} -t ${params.TENANTID} -r ${params.ROLE} -a \"${params.APPNAME}\" -b ${params.BROWSER} -f cloud_config_start_apps.json -g ${params.GEO} -l ${params.LICENSE} -m ${params.MODEL}"
            }//steps
        }//stage
        stage("Smoke Test Execution") {
            steps {
                catchError (buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    retry(1) {
                        script {
                            errorcheck = fileExists('d:/temp/found.txt')
                            if (errorcheck) {
                                bat "rm d:\\temp\\found.txt"
                                println 'Error check File Removed'
                            }
                            cnext_file_check = fileExists('d:/wdir/traces/cnext_client.out')
                            if (cnext_file_check) {
                                bat "rm d:\\wdir\\traces\\cnext_client.out"
                                println 'Cnext File Removed'
                            }
                        }

                        bat tck+tckProfile+sikuliRun
                        bat "findstr /B \"E  t\" d:\\temp\\out > NUL && echo String was found >d:\\temp\\found.txt || echo String not found "
                        script {
                            errorcheck = fileExists('d:/temp/found.txt')
                            if (errorcheck) {
                                println 'Error in one of the step'
                                bat "type d:\\temp\\out"
                                bat "rm d:\\temp\\found.txt"
                                bat 'exit 1'
                            } else {
                                println 'Stage Succesful'
                            }
                        }//script
                    } //retry
                }//catcherror
            }//step
            post {
                success {
                    echo "${ROLE} Smoke Test Stage successful"
                }
                failure {
                    echo "${ROLE} Smoke Test Stage Failed"
                    error "!!!! ${ROLE} Smoke Test Failed !!!!"
                }
            }
        }//stage
    }//stages
}//pipeline