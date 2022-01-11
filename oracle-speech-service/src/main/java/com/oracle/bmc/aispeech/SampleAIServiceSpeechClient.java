package com.oracle.bmc.aispeech;

import com.oracle.bmc.aispeech.model.CreateTranscriptionJobDetails;
import com.oracle.bmc.aispeech.model.InputLocation;
import com.oracle.bmc.aispeech.model.ObjectListInlineInputLocation;
import com.oracle.bmc.aispeech.model.ObjectLocation;
import com.oracle.bmc.aispeech.model.OutputLocation;
import com.oracle.bmc.aispeech.model.TranscriptionJob;
import com.oracle.bmc.aispeech.model.TranscriptionJobCollection;
import com.oracle.bmc.aispeech.model.TranscriptionModelDetails;
import com.oracle.bmc.aispeech.model.TranscriptionTask;
import com.oracle.bmc.aispeech.model.TranscriptionTaskCollection;
import com.oracle.bmc.aispeech.requests.CreateTranscriptionJobRequest;
import com.oracle.bmc.aispeech.requests.GetTranscriptionJobRequest;
import com.oracle.bmc.aispeech.requests.GetTranscriptionTaskRequest;
import com.oracle.bmc.aispeech.requests.ListTranscriptionJobsRequest;
import com.oracle.bmc.aispeech.requests.ListTranscriptionTasksRequest;
import com.oracle.bmc.aispeech.responses.CreateTranscriptionJobResponse;
import com.oracle.bmc.aispeech.responses.GetTranscriptionJobResponse;
import com.oracle.bmc.aispeech.responses.GetTranscriptionTaskResponse;
import com.oracle.bmc.aispeech.responses.ListTranscriptionJobsResponse;
import com.oracle.bmc.aispeech.responses.ListTranscriptionTasksResponse;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;

import java.io.IOException;
import java.util.List;


public class SampleAIServiceSpeechClient {

    private static AIServiceSpeechClient client;

    public SampleAIServiceSpeechClient() throws IOException {

        // Details of Config File, set to DEFAULT for Default location of config file
        ConfigFileAuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider("DEFAULT");
        client = new AIServiceSpeechClient(provider);
        System.out.println("Client was created.");
    }

    public static void main(String... args) throws IOException {

        SampleAIServiceSpeechClient sampleAIServiceSpeechClient = new SampleAIServiceSpeechClient();

        // This method call creates Transcription Job and User need to provide Job details in createTranscriptionJobDetails method
        TranscriptionJob transcriptionJob = createTranscriptionJob(createTranscriptionJobDetails());
        System.out.println(transcriptionJob);

        // This method call gets Transcription Job with TranscriptionJob Id of job created
        transcriptionJob = getTranscriptionJob(transcriptionJob.getId());
        System.out.println(transcriptionJob);

        // This call lists all Transcription Jobs in compartment of created job's compartment
        TranscriptionJobCollection transcriptionJobCollection = listTranscriptionJob(transcriptionJob.getCompartmentId());
        System.out.println(transcriptionJobCollection);

        // Please wait for some time for Transcription tasks to be created under Transcription Job created
        try {
            Thread.sleep(15_000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // This method call gets transcription tasks under created job
        TranscriptionTaskCollection transcriptionTaskCollection = listTranscriptionTask(transcriptionJob.getId());
        System.out.println(transcriptionTaskCollection);

        // This method call gets 1st transcription task from list of Transcription tasks under created Job
        if (!transcriptionTaskCollection.getItems().isEmpty()) {
            TranscriptionTask transcriptionTask = getTranscriptionTask(transcriptionJob.getId(), transcriptionTaskCollection.getItems().get(0).getId());
            System.out.println(transcriptionTask);
        } else {
            System.out.println("No tasks created yet!");
        }

    }

    // This method creates a transcription job by calling createTranscriptionJobDetails method by using the details provided in createTranscriptionJobDetails method
    private static TranscriptionJob createTranscriptionJob(CreateTranscriptionJobDetails transcriptionJobDetails) {

        CreateTranscriptionJobRequest request = CreateTranscriptionJobRequest.builder().createTranscriptionJobDetails(transcriptionJobDetails).build();

        CreateTranscriptionJobResponse response = client.createTranscriptionJob(request);

        if (response.get__httpStatusCode__() != 200) {
            throw new RuntimeException("Error occurred during create operation");
        }

        return response.getTranscriptionJob();
    }

    // This method creates the Transcription Job. Please enter Job details for the job creation.
    private static CreateTranscriptionJobDetails createTranscriptionJobDetails() {

        // Change this id to the compartment ID in which you want to create Transcription Job
        // This need to match the tenancy=ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda
        // in ~/.oci/config
        String compartmentId = "ocid1.tenancy.oc1..aaaaaaaavhztk6bkuogd5w3nufs5dzts6dfob4nqxedvgbsi7qadonat76fa";
                               // "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda";

        // Give your job a name here
        String jobName = "sampleJob";

        // Give description for your job here
        String description = "This is newly created Job";

        // Change this namespace to your job's namespace and give object names
        ObjectLocation objectLocation = ObjectLocation.builder()
                .namespaceName("axsjzgvicq5h") // ("") // axsjzgvicq5h
                .bucketName("speech_test") // ("olediour-20220107-1433") // speech_test
                .objectNames(List.of("adaml-test-3chunk.wav")).build(); // foo.wav
        InputLocation inlineInputLocation = ObjectListInlineInputLocation.builder().objectLocations(List.of(objectLocation)).build();

        // Change this namespace to your job's namespace and change this sample bucketName to your bucket name and you can enter name of job in prefix
        OutputLocation outputLocation = OutputLocation.builder()
                .namespaceName("axsjzgvicq5h") // ("") // axsjzgvicq5h
                .bucketName("speech_test") // ""olediour-20220107-1433") // speech_test
                .prefix("JAVA_SDK_DEMO/").build();

        // Transcription Mode details can be provided here
        TranscriptionModelDetails modelDetails = TranscriptionModelDetails.builder().domain(TranscriptionModelDetails.Domain.Generic)
                .languageCode(TranscriptionModelDetails.LanguageCode.EnUs).build();

        // For now only the above values are supported for Transcription Job creation


        return CreateTranscriptionJobDetails.builder().compartmentId(compartmentId).displayName(jobName).description(description)
                .inputLocation(inlineInputLocation).outputLocation(outputLocation).modelDetails(modelDetails).build();
    }

    // This method returns details of Transcription job with the provided transcriptionJobId
    private static TranscriptionJob getTranscriptionJob(String transcriptionJobId) {

        GetTranscriptionJobRequest request = GetTranscriptionJobRequest.builder().transcriptionJobId(transcriptionJobId).build();
        GetTranscriptionJobResponse response = client.getTranscriptionJob(request);

        if (response.get__httpStatusCode__() != 200) {
            throw new RuntimeException("Error occurred during getting transcription Job");
        }

        return response.getTranscriptionJob();
    }

    // This method lists the transcription Jobs in given compartment
    private static TranscriptionJobCollection listTranscriptionJob(String compartmentId) {
        ListTranscriptionJobsRequest listTranscriptionJobsRequest = ListTranscriptionJobsRequest.builder().compartmentId(compartmentId).build();
        ListTranscriptionJobsResponse response = client.listTranscriptionJobs(listTranscriptionJobsRequest);

        if (response.get__httpStatusCode__() != 200) {
            throw new RuntimeException("Error occurred during listing of transcription Jobs in provided compartment");
        }

        return response.getTranscriptionJobCollection();
    }

    // This method lists the transcription tasks under provided transcriptionJobId
    private static TranscriptionTaskCollection listTranscriptionTask(String transcriptionJobId) {
        ListTranscriptionTasksRequest listTranscriptionJobsRequest = ListTranscriptionTasksRequest.builder().transcriptionJobId(transcriptionJobId).build();
        ListTranscriptionTasksResponse response = client.listTranscriptionTasks(listTranscriptionJobsRequest);

        if (response.get__httpStatusCode__() != 200) {
            throw new RuntimeException("Error occurred during listing transcription tasks under given transcription job");
        }

        return response.getTranscriptionTaskCollection();
    }

    // This method returns the Transcription Task with the provided transcriptionTaskId under transcriptionJobId
    private static TranscriptionTask getTranscriptionTask(String transcriptionJobId, String transcriptionTaskId) {
        GetTranscriptionTaskRequest getTranscriptionTaskRequest = GetTranscriptionTaskRequest.builder().transcriptionJobId(transcriptionJobId).transcriptionTaskId(transcriptionTaskId).build();
        GetTranscriptionTaskResponse response = client.getTranscriptionTask(getTranscriptionTaskRequest);

        if (response.get__httpStatusCode__() != 200) {
            throw new RuntimeException("Error occurred during getting transcription task under provided transcriptionJobId");
        }

        return response.getTranscriptionTask();
    }


}