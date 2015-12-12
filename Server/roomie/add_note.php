<?php
require_once 'include/DB_Functions.php';
$db = new DB_Functions();

// json response array
$response = array("error" => FALSE);

if (isset($_POST['email']) && isset($_POST['api_key']) && isset($_POST['note_description'])) {

    // receiving the post params
    $email = $_POST['email'];
    $api_key = $_POST['api_key'];
    $note_description = $_POST['note_description'];

    if ($db->isValidApiKey($api_key, $email)) {

        $houseID = $db->getHouseID($email);

        if ($houseID != NULL) {
            $note = $db->addNote($houseID, $note_description, $email);
            if ($note) {
                $db->updateHouseLastUpdated($houseID);
                $response["note"]["id"] = $note["noteID"];
                $response["note"]["houseId"] = $note["houseID"];
                $response["note"]["name"] = $note["name"];
                $response["note"]["created_at"] = $note["created_at"];
                $response["note"]["last_updated"] = $note["last_updated"];
                $response["error"] = FALSE;
                echo json_encode($response);
            } else {
                $response["error"] = TRUE;
                $response["error_msg"] = "Unknown Error!";
                echo json_encode($response);
            }
        } else {
            $response["error"] = TRUE;
            $response["error_msg"] = "User has no house!";
            echo json_encode($response);
        }
    } else {
        $response["error"] = TRUE;
        $response["error_msg"] = "Invalid API Key!";
        echo json_encode($response);
    }
} else {
    // required post params is missing
    $response["error"] = TRUE;
    $response["error_msg"] = "Required parameters email, api_key or note_description is missing!";
    echo json_encode($response);
}
?>
