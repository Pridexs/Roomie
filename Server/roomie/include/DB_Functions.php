<?php

class DB_Functions {

    private $conn;

    // constructor
    function __construct() {
        require_once 'DB_Connect.php';
        // connecting to database
        $db = new Db_Connect();
        $this->conn = $db->connect();
    }

    // destructor
    function __destruct() {

    }

    /**
     * Storing new user
     * returns user details
     */
    public function storeUser($name, $email, $password) {
        $hash = $this->hashSSHA($password);
        $encrypted_password = $hash["encrypted"]; // encrypted password
        $salt = $hash["salt"]; // salt
        $api_key = $this->generateApiKey();

        $stmt = $this->conn->prepare("INSERT INTO users(api_key, name, email, password, salt) VALUES(?, ?, ?,  ?, ?)");
        $stmt->bind_param("sssss", $api_key, $name, $email, $encrypted_password, $salt);
        $result = $stmt->execute();
        $stmt->close();

        // check for successful store
        if ($result) {
            $stmt = $this->conn->prepare("SELECT * FROM users WHERE email = ?");
            $stmt->bind_param("s", $email);
            $stmt->execute();
            $user = $stmt->get_result()->fetch_assoc();
            $stmt->close();

            return $user;
        } else {
            return false;
        }
    }

    /**
     * Get user by email and password
     */
    public function getUserByEmailAndPassword($email, $password) {

        $stmt = $this->conn->prepare("SELECT * FROM users WHERE email = ?");

        $stmt->bind_param("s", $email);

        if ($stmt->execute()) {
            $user = $stmt->get_result()->fetch_assoc();
            if ($user['password'] == $this->checkhashSSHA($user['salt'], $password)) {
                $stmt->close();
                return $user;
            } else {
                return NULL;
            }
        } else {
            return NULL;
        }
    }

    /**
     * Check user is existed or not
     */
    public function isUserExisted($email) {
        $stmt = $this->conn->prepare("SELECT email from users WHERE email = ?");
        $stmt->bind_param("s", $email);
        $stmt->execute();
        $stmt->store_result();

        if ($stmt->num_rows > 0) {
            // user existed
            $stmt->close();
            return true;
        } else {
            // user not existed
            $stmt->close();
            return false;
        }
    }

    /**
     * Register new house
     * returns house details
     */
    public function registerHouse($house_name, $house_password) {
        $hash = $this->hashSSHA($house_password);
        $encrypted_password = $hash["encrypted"]; // encrypted password
        $salt = $hash["salt"]; // salt

        $stmt = $this->conn->prepare("INSERT INTO house(name, password, salt, last_updated) VALUES(?, ?, ?, NOW())");
        $stmt->bind_param("sss", $house_name, $encrypted_password, $salt);
        $result = $stmt->execute();

        $id = $this->conn->insert_id;

        $stmt->close();
        // check for successful store
        if ($result) {
            $stmt = $this->conn->prepare("SELECT * FROM house WHERE houseID = ?");
            $stmt->bind_param("s", $id);
            $stmt->execute();
            $house = $stmt->get_result()->fetch_assoc();
            $stmt->close();

            return $house;
        } else {
            return false;
        }
    }

    public function updateHousePassword($houseId, $house_password) {
        $hash = $this->hashSSHA($house_password);
        $encrypted_password = $hash["encrypted"]; // encrypted password
        $salt = $hash["salt"]; // salt

        $stmt = $this->conn->prepare("UPDATE house SET password = ?, salt = ? WHERE houseID = ?");
        $stmt->bind_param("ssi", $encrypted_password, $salt, $houseId);
        $result = $stmt->execute();

        $id = $this->conn->insert_id;

        $stmt->close();
        // check for successful store
        if ($result) {
            $stmt = $this->conn->prepare("SELECT * FROM house WHERE houseID = ?");
            $stmt->bind_param("s", $id);
            $stmt->execute();
            $house = $stmt->get_result()->fetch_assoc();
            $stmt->close();

            return $house;
        } else {
            return false;
        }
    }

    /**
     * joinHouse
     */
    public function getHouseByIdAndPassword($house_id, $house_password) {
        $stmt = $this->conn->prepare("SELECT * FROM house WHERE houseID = ?");
        $stmt->bind_param("i", $house_id);
        $house = $stmt->execute();

        if ($house) {
            $house = $stmt->get_result()->fetch_assoc();
            if ($house['password'] == $this->checkhashSSHA($house['salt'], $house_password)) {
                $stmt->close();
                return $house;
            } else {
                return NULL;
            }
        } else {
            return NULL;
        }

        // check for successful store
        if ($result) {
            $stmt = $this->conn->prepare("SELECT * FROM house WHERE houseID = ?");
            $stmt->bind_param("s", $id);
            $stmt->execute();
            $house = $stmt->get_result()->fetch_assoc();
            $stmt->close();

            return $house;
        } else {
            return false;
        }
    }

    public function registerUserInHouse($house_id, $email, $isAdmin) {
        $stmt = $this->conn->prepare("INSERT INTO house_member(houseID, email, isAdmin) VALUES(?, ?, ?)");
        $stmt->bind_param("ssi", $house_id, $email, $isAdmin);
        $result = $stmt->execute();
        $stmt->close();

        // check for successful store
        if ($result) {
            // Updates the last_updated field
            $stmt = $this->conn->prepare("UPDATE house SET last_updated = NOW() WHERE houseID = ?");
            $stmt->bind_param("i", $house_id);
            $stmt->execute();
            $stmt->close();

            $stmt = $this->conn->prepare("SELECT * FROM house_member WHERE houseID = ? AND email = ?");
            $stmt->bind_param("ss", $house_id, $email);
            $stmt->execute();
            $house_member = $stmt->get_result()->fetch_assoc();
            $stmt->close();

            return $house_member;
        } else {
            return false;
        }
    }

    /**
     * Get House ID
     */
    public function getHouseID($email) {
        $stmt = $this->conn->prepare("SELECT houseID FROM house_member WHERE email = ?");
        $stmt->bind_param("s", $email);

        if ($stmt->execute()) {
            $house = $stmt->get_result()->fetch_assoc();
            $stmt->close();
            return $house["houseID"];
        } else {
            return NULL;
        }
    }

    /**
     * Get Members of House
     */
    public function getMembersHouse($houseID) {
        $stmt = $this->conn->prepare("SELECT u.name, u.email, hm.isAdmin, houseID FROM house_member as hm INNER JOIN users as u ON hm.email = u.email WHERE houseID = ?");
        $stmt->bind_param("s", $houseID);

        if ($stmt->execute()) {
            $house = $stmt->get_result();
            $stmt->close();
            return $house;
        } else {
            return NULL;
        }
    }

    public function deleteHouseMember($houseID, $email) {
        $stmt = $this->conn->prepare("DELETE FROM house_member WHERE houseID = ? AND email = ?");
        $stmt->bind_param("is", $houseID, $email);
        $stmt->execute();
        $stmt->close();
    }

    /**
     * Get Notes from House
     */
    public function getNotes($houseID, $lastUpdated) {
        $stmt = $this->conn->prepare("SELECT * FROM note WHERE houseID = ? AND last_updated > ?");
        $stmt->bind_param("ss", $houseID, $lastUpdated);

        if ($stmt->execute()) {
            $notes = $stmt->get_result();
            $stmt->close();
            return $notes;
        } else {
            return NULL;
        }
    }

    /**
     * Get Specific Note from House
     */
    public function getNote($noteId) {
        $stmt = $this->conn->prepare("SELECT * FROM note WHERE noteID = ?");
        $stmt->bind_param("i", $noteID);

        if ($stmt->execute()) {
            $note = $stmt->get_result()->fetch_assoc();
            $stmt->close();
            return $note;
        } else {
            return NULL;
        }
    }

    /**
     * Remove Note from House
     */
    public function deleteNote($noteId) {
        $stmt = $this->conn->prepare("UPDATE note SET was_deleted = 1, last_updated = NOW() WHERE noteID = ?");
        $stmt->bind_param("i", $noteId);
        $stmt->execute();
        $stmt->close();
    }

    /**
     * Add Note from House
     */
    public function addNote($houseId, $noteDescription, $createdBy) {
        $stmt = $this->conn->prepare("INSERT INTO note(houseID, createdBy, description) VALUES (?, ?, ?)");
        $stmt->bind_param("sss", $houseId, $createdBy, $noteDescription);
        $result = $stmt->execute();
        $id = $this->conn->insert_id;
        if ($result) {
            $stmt = $this->conn->prepare("SELECT * FROM note WHERE noteID = ?");
            $stmt->bind_param("s", $id);
            $stmt->execute();
            $note = $stmt->get_result()->fetch_assoc();
            $stmt->close();

            return $note;
        } else {
            return false;
        }
    }

    public function updateNote($noteId, $noteDescription) {
        $stmt = $this->conn->prepare("UPDATE note SET description = ?, last_updated = NOW() WHERE noteID = ?");
        $stmt->bind_param("ss", $noteDescription, $noteId);
        $result = $stmt->execute();
        if ($result) {
            $stmt = $this->conn->prepare("SELECT * FROM note WHERE noteID = ?");
            $stmt->bind_param("s", $noteId);
            $stmt->execute();
            $note = $stmt->get_result()->fetch_assoc();
            $stmt->close();
            return $note;
        } else {
            return false;
        }
    }

    public function deleteAllNotesFromUser($email, $houseId) {
        $stmt = $this->conn->prepare("UPDATE note SET was_deleted = 1 WHERE createdBy = ? AND houseID = ?");
        $stmt->bind_param("si", $email, $houseId);
        $stmt->execute();
        $stmt->close();
    }

    /**
     * Get House Information
     */
    public function getHouseInfo($houseID) {
        $stmt = $this->conn->prepare("SELECT name, last_updated FROM house WHERE houseID = ?");
        $stmt->bind_param("s", $houseID);

        if ($stmt->execute()) {
            $house = $stmt->get_result()->fetch_assoc();
            $stmt->close();
            return $house;
        } else {
            return NULL;
        }
    }

    /**
     * Encrypting password
     * @param password
     * returns salt and encrypted password
     */
    public function hashSSHA($password) {

        $salt = sha1(rand());
        $salt = substr($salt, 0, 10);
        $encrypted = base64_encode(sha1($password . $salt, true) . $salt);
        $hash = array("salt" => $salt, "encrypted" => $encrypted);
        return $hash;
    }

    /**
     * Decrypting password
     * @param salt, password
     * returns hash string
     */
    public function checkhashSSHA($salt, $password) {

        $hash = base64_encode(sha1($password . $salt, true) . $salt);

        return $hash;
    }

    /**
     * Generating random Unique MD5 String for user Api key
     */
    public function generateApiKey() {
        return md5(uniqid(rand(), true));
    }

    /*/
     * Checks if the API is valid for that user
     */
    public function isValidApiKey($api_key, $email) {
        $stmt = $this->conn->prepare("SELECT email from users WHERE api_key = ? AND email = ?");
        $stmt->bind_param("ss", $api_key, $email);
        $stmt->execute();
        $stmt->store_result();
        $num_rows = $stmt->num_rows;
        $stmt->close();
        return $num_rows > 0;
    }

    /*/
     * Checks if the User is admin
     */
    public function isUserAdmin($houseId, $email) {
        $stmt = $this->conn->prepare("SELECT * from house_member WHERE houseId = ? AND email = ? AND isAdmin = 1");
        $stmt->bind_param("ss", $houseId, $email);
        $stmt->execute();
        $stmt->store_result();
        $num_rows = $stmt->num_rows;
        $stmt->close();
        return $num_rows > 0;
    }

    public function updateHouseLastUpdated($houseId) {
        $stmt = $this->conn->prepare("UPDATE house SET last_updated = NOW() WHERE houseID = ?");
        $stmt->bind_param("i", $houseId);
        $stmt->execute();
        $stmt->close();
    }

}

?>
