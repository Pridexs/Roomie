<?php
require_once 'include/DB_Functions.php';
$db = new DB_Functions();

// json response array
$response = array("error" => FALSE);

if (isset($_POST['email']) && isset($_POST['api_key']) && isset($_POST['note_id'])) {

    // receiving the post params
    $email = $_POST['email'];
    $api_key = $_POST['api_key'];
    $note_id = $_POST['note_id'];

    if ($db->isValidApiKey($api_key, $email)) {
        $houseID = $db->getHouseID($email);
        if ($houseID != NULL) {
            $response["valid_house"] = TRUE;
            if ($db->isUserAdmin($houseID, $email)) {
                $db->deleteNote($note_id);
                $db->updateHouseLastUpdated($note_id);
            } else {
                $response["error"] = TRUE;
                $response["error_msg"] = "You are not an admin!";
                echo json_encode($response);
            }
            echo json_encode($response);
        } else {
            $response["valid_house"] = FALSE;
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
    $response["error_msg"] = "Required parameters email, api_key or note_id is missing!";
    echo json_encode($response);
}
?>
