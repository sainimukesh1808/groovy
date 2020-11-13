/*
This is the master groovy script which will
invoke the individual role smoke tests through
parameters supplied
Author : XKK / MSI26
*/

// The below variables needs to be changed for every pod

def roles = "${params.ROLE}".trim().split(',')
def pod_url = "${params.PODURL}"
def password = "${params.PASSWORD}"
def pod_id = "${params.PODID}"
def browser = "${params.BROWSER}"
def tenant_id = "${params.TENANTID}"
def geo = "${params.GEO}"

//Builds to Run, provide the list as per the list given below
//['Base URL','UserName','Password','email','Display Name','PodID','Tenant ID','Geo','Role','App Name','Browser','License','Model Name']
build_list = [:]

build_list['IME']=[pod_url,'ime_euw1', password,'ime_euw1@dispostable.com','IME EUW1',pod_id,tenant_id,geo,'IME','Plastic Injection',browser,'None','Plastic_IME.3dxml']
build_list['SRD']=[pod_url,'srd_euw1', password,'srd_euw1@dispostable.com','SRD EUW1',pod_id,tenant_id,geo,'SRD','Linear Structural Validation',browser,'Credits','SRD_Structural_Linear.3dxml']
build_list['SLL']=[pod_url,'sll_euw1', password,'sll_euw1@dispostable.com','SLL EUW1',pod_id,tenant_id,geo,'SLL','Linear Structural Scenario Creation',browser,'Tokens','SLL_SolidworksPartSimulation_Structural.3dxml']
build_list['SNE']=[pod_url,'sne_euw1', password,'sne_euw1@dispostable.com','SNE EUW1',pod_id,tenant_id,geo,'SNE','Mechanical Scenario Creation',browser,'Credits','SNE_Expilicit_DoubleBoth.3dxml']
build_list['CYE']=[pod_url,'cye_euw1', password,'cye_euw1@dispostable.com','CYE EUW1',pod_id,tenant_id,geo,'CYE','Structural Scenario Creation',browser,'Credits','DRD_Cantilever.3dxml']
build_list['TIRUI']=[pod_url,'tirui_euw1', password,'tirui_euw1@dispostable.com','TIRUI EUW1',pod_id,tenant_id,geo,'TIRUI','Mechanical Scenario Creation',browser,'Credits','TIRUI_CGM.3dxml']
build_list['PAE']=[pod_url,'pae_euw1', password,'pae_euw1@dispostable.com','PAE EUW1',pod_id,tenant_id,geo,'PAE','Mechanical Scenario Creation',browser,'Tokens','PAE_ThermalStructural_BatchMesh.3dxml']
build_list['SYE']=[pod_url,'sye_euw1', password,'sye_euw1@dispostable.com','SYE EUW1',pod_id,tenant_id,geo,'SYE','Mechanical Scenario Creation',browser,'Credits','Explicit_Knuckle.3dxml']
build_list['SFO']=[pod_url,'sfo_euw1', password,'sfo_euw1@dispostable.com','SFO EUW1',pod_id,tenant_id,geo,'SFO','Structural Scenario Creation',browser,'Credits','DRD_Cantilever.3dxml']
build_list['SSEMO']=[pod_url,'ssemo_euw1', password,'ssemo_euw1@dispostable.com','SSEMO EUW1',pod_id,tenant_id,geo,'SSEMO','Structural Scenario Creation',browser,'Credits','DRD_Cantilever.3dxml']
build_list['SSU']=[pod_url,'ssu_euw1', password,'ssu_euw1@dispostable.com','SSU EUW1',pod_id,tenant_id,geo,'SSU','Mechanical Scenario Creation',browser,'Tokens','SSU_Explicit.3dxml']
build_list['AMF']=[pod_url,'amf_euw1', password,'amf_euw1@dispostable.com','AMF EUW1',pod_id,tenant_id,geo,'AMF','Additive Manufacturing Scenario Creation',browser,'Tokens','AMF_Smoke_Test.3dxml']
build_list['TME']=[pod_url,'tme_euw1', password,'tme_euw1@dispostable.com','TME EUW1',pod_id,tenant_id,geo,'TME','Fluid Scenario Creation',browser,'Tokens','TME_Fluids.3dxml']
build_list['CTEAD']=[pod_url,'ctead_euw1', password,'ctead_euw1@dispostable.com','CTEAD EUW1',pod_id,tenant_id,geo,'CTEAD','Fluid Scenario Creation',browser,'Tokens','CTEAD_Catalytic_Converter.3dxml']
build_list['AHEUA']=[pod_url,'aheua_euw1', password,'aheua_euw1@dispostable.com','AHEUA EUW1',pod_id,tenant_id,geo,'AHEUA','Fluid Scenario Creation',browser,'Tokens','AHEUA_Solid_Radiation.3dxml']
build_list['FMK']=[pod_url,'fmk_euw1', password,'fmk_euw1@dispostable.com','FMK EUW1',pod_id,tenant_id,geo,'FMK','Fluid Scenario Creation',browser,'Tokens','FMK_Fluids.3dxml']
build_list['SGD']=[pod_url,'sgd_euw1', password,'sgd_euw1@dispostable.com','SGD EUW1',pod_id,tenant_id,geo,'SGD','Functional Gen. Design',browser,'Credits','SGD_Smoke_Test.3dxml']

for(int i=0; i < roles.size(); i++) {
    stage("Role - "+build_list[roles[i]][8]){
        catchError (buildResult: 'SUCCESS', stageResult: 'FAILURE') {
            retry(2) {
                build job: 'Smoke_Test_On_The_Cloud_PUN02',
                parameters: [
                    string(name :'BASEURL' , value : build_list[roles[i]][0]),
                    string(name :'USER_NAME' , value : build_list[roles[i]][1]),
                    string(name :'PASSWORD' , value : build_list[roles[i]][2]),
                    string(name :'EMAIL' , value : build_list[roles[i]][3]),
                    string(name :'USERDISPLAYNAME' , value : build_list[roles[i]][4]),
                    string(name :'PODID' , value : build_list[roles[i]][5]),
                    string(name :'TENANTID' , value : build_list[roles[i]][6]),
                    string(name :'GEO' , value : build_list[roles[i]][7]),
                    string(name :'ROLE' , value : build_list[roles[i][8]),
                    string(name :'APPNAME' , value : build_list[roles[i]][9]),
                    string(name :'BROWSER' , value : build_list[roles[i]][10]),
                    string(name :'LICENSE' , value : build_list[roles[i]][11]),
                    string(name :'MODEL' , value : build_list[roles[i]][12]),
                ]
                propagate: false
            }//retry
        }//catchError
    }//stage
}//for loop