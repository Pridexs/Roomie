<?php
require_once 'include/DB_Functions.php';
$db = new DB_Functions();

// json response array
$response = array("error" => FALSE);

if (isset($_POST['email']) && isset($_POST['api_key']) && isset($_POST['last_updated'])) {

    // receiving the post params
    $email = $_POST['email'];
    $api_key = $_POST['api_key'];
    $last_updated = $_POST['last_updated'];

    if ($db->isValidApiKey($api_key, $email)) {
        $houseID = $db->getHouseID($email);

        if ($houseID != NULL) {
            $houseInfo = $db->getHouseInfo($houseID);

            $response["valid_house"] = TRUE;
            $response["house_id"] = $houseID;
            $response["house_name"] = $houseInfo["name"];
            $response["last_updated"] = $houseInfo["last_updated"];

            if ($last_updated < $houseInfo["last_updated"]) {
                $response["requires_sync"] = TRUE;
                $count = 0;
                $house_members = $db->getMembersHouse($houseID);
                $members = array();
                while ($row = $house_members->fetch_assoc()) {
					$member["name"] = $row["name"];
                    $member["email"] = $row["email"];
                    $member["isAdmin"] = $row["isAdmin"];
                    array_push($members, $member);
                }
                $response["members"] = $members;
            } else {
                $response["requires_sync"] = FALSE;
            }

            $notes = $db->getNotes($houseID, $last_updated);
            $notesArray = array();
            if ($notes != NULL) {
                while ($row = $notes->fetch_assoc()) {
    				$note["noteID"] = $row["noteID"];
                    $note["createdBy"] = $row["createdBy"];
                    $note["name"] = $row["name"];
                    $note["description"] = $row["description"];
                    $note["created_at"] = $row["created_at"];
                    $note["last_updated"] = $row["last_updated"];
                    $note["was_deleted"] = $row["was_deleted"];
                    array_push($notesArray, $note);
                }
                $response["notes"] = $notesArray;
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
    $response["error_msg"] = "Required parameters email, password or last_updated is missing!";
    echo json_encode($response);
}
?>
